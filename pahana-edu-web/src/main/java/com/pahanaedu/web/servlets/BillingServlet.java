package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet(name = "BillingServlet", urlPatterns = {"/billing"})
public class BillingServlet extends HttpServlet {

    // NEW: Guest defaults
    private static final int GUEST_CUSTOMER_ID = 1;
    private static final String GUEST_CUSTOMER_NAME = "Walk-in Customer";

    private String apiBase;

    @Override
    public void init() {
        this.apiBase = getServletContext().getInitParameter("apiBase");
        if (this.apiBase == null) {
            this.apiBase = "http://localhost:8080/pahana-edu-service/api";
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ApiClient api = new ApiClient(apiBase);

        // ---- Customers -> List<Map> (id, accountNumber, name)
        JsonArray custArr = api.getJson("/customers").asJsonArray();
        List<Map<String, Object>> customers = new ArrayList<>();
        for (int i = 0; i < custArr.size(); i++) {
            JsonObject o = custArr.getJsonObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.valueOf(o.getInt("id")));
            m.put("accountNumber", o.getString("accountNumber", ""));
            m.put("name", o.getString("name", ""));
            // Optional: skip showing the Guest in the dropdown
            // if (Objects.equals(m.get("id"), GUEST_CUSTOMER_ID)) continue;
            customers.add(m);
        }

        // ---- Items -> List<Map> (id, sku, name, unitPrice BigDecimal)
        JsonArray itemArr = api.getJson("/items").asJsonArray();
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < itemArr.size(); i++) {
            JsonObject o = itemArr.getJsonObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.valueOf(o.getInt("id")));
            m.put("sku", o.getString("sku", ""));
            m.put("name", o.getString("name", ""));
            BigDecimal price = o.isNull("unitPrice")
                    ? BigDecimal.ZERO
                    : o.getJsonNumber("unitPrice").bigDecimalValue();
            m.put("unitPrice", price);
            items.add(m);
        }

        req.setAttribute("customers", customers);
        req.setAttribute("items", items);
        req.getRequestDispatcher("/bills/create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ApiClient api = new ApiClient(apiBase);

        // ----- Read form
        String customerIdStr = req.getParameter("customerId");
        String[] itemIds     = req.getParameterValues("itemId");
        String[] qtys        = req.getParameterValues("qty");
        String[] unitPrices  = req.getParameterValues("unitPrice");

        // CHANGED: default to Guest when empty (no redirect)
        int customerId;
        if (customerIdStr == null || customerIdStr.isBlank()) {
            customerId = GUEST_CUSTOMER_ID;  // Walk-in
        } else {
            customerId = Integer.parseInt(customerIdStr);
        }

        // ----- Build JSON payload for the service
        JsonArrayBuilder lines = Json.createArrayBuilder();
        if (itemIds != null) {
            for (int i = 0; i < itemIds.length; i++) {
                String idStr = itemIds[i];
                if (idStr == null || idStr.isBlank()) continue;

                int qty = (qtys != null && i < qtys.length && qtys[i] != null && !qtys[i].isBlank())
                        ? Integer.parseInt(qtys[i]) : 1;

                BigDecimal price = (unitPrices != null && i < unitPrices.length
                        && unitPrices[i] != null && !unitPrices[i].isBlank())
                        ? new BigDecimal(unitPrices[i].trim()) : BigDecimal.ZERO;

                lines.add(Json.createObjectBuilder()
                        .add("itemId", Integer.parseInt(idStr))
                        .add("qty", qty)
                        .add("unitPrice", price));
            }
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("customerId", customerId)
                .add("items", lines)
                .build();

        Map<String, Object> billVM = null;

        // 1) Try POST expecting JSON back
        JsonObject createdObj = null;
        try {
            JsonStructure js = api.sendJsonForJson("POST", "/bills", payload.toString());
            if (js != null && js.getValueType() == JsonValue.ValueType.OBJECT) {
                createdObj = js.asJsonObject();
            }
        } catch (IOException ignore) {
            // service may return 204; we'll try other options below
        }

        // 2) If no JSON came back, try POST without expecting a body (for 204 responses)
        if (createdObj == null) {
            try {
                api.sendJsonNoBody("POST", "/bills", payload.toString());
            } catch (IOException ignore) {
                // keep going; we'll still try to show something
            }
        }

        // 3) If we have a full bill with items -> use it
        if (createdObj != null
                && createdObj.containsKey("items")
                && !createdObj.isNull("items")
                && createdObj.getJsonArray("items").size() > 0) {
            billVM = toViewModel(createdObj);
        }

        // 4) Otherwise: read back newest bill for this customer
        if (billVM == null) {
            try {
                JsonArray list = Json.createArrayBuilder().build();

                // Prefer a filtered endpoint if your API supports it
                try {
                    JsonStructure js = api.getJson("/bills?customerId=" + customerId);
                    if (js instanceof JsonArray) list = js.asJsonArray();
                } catch (Exception ignored) { }

                // If nothing came from the filtered endpoint, fallback to /bills
                if (list.isEmpty()) {
                    try {
                        JsonStructure js = api.getJson("/bills");
                        if (js instanceof JsonArray) list = js.asJsonArray();
                    } catch (Exception ignored) { }
                }

                JsonObject newestForCustomer = pickNewestForCustomer(list, customerId);
                if (newestForCustomer != null
                        && newestForCustomer.containsKey("items")
                        && !newestForCustomer.isNull("items")) {
                    billVM = toViewModel(newestForCustomer);
                }
            } catch (Exception ignored) {
                // continue to fallback
            }
        }

        // 5) Last resort: local VM (TMP- preview)
        if (billVM == null) {
            billVM = buildLocalViewModel(api, customerId, itemIds, qtys, unitPrices);
            req.setAttribute("unsavedNotice",
                    "This invoice preview was not returned by the server (TMP-). Reports will not include it.");
        }

        req.setAttribute("bill", billVM);
        req.getRequestDispatcher("/bills/view.jsp").forward(req, resp);
    }

    /** Build bill locally when service response is empty/minimal. */
    private Map<String, Object> buildLocalViewModel(ApiClient api,
                                                    int customerId,
                                                    String[] itemIds,
                                                    String[] qtys,
                                                    String[] unitPrices) throws IOException {

        // Index all items by id for quick lookup
        Map<Integer, JsonObject> itemIndex = new HashMap<>();
        JsonArray allItems = api.getJson("/items").asJsonArray();
        for (int i = 0; i < allItems.size(); i++) {
            JsonObject it = allItems.getJsonObject(i);
            itemIndex.put(it.getInt("id"), it);
        }

        // Try to get customer name
        String customerName = null;
        try {
            JsonObject c = api.getJson("/customers/" + customerId).asJsonObject();
            customerName = c.getString("name", null);
        } catch (Exception ignored) {}

        // NEW: if guest and no name fetched, show a friendly default
        if ((customerName == null || customerName.isBlank()) && customerId == GUEST_CUSTOMER_ID) {
            customerName = GUEST_CUSTOMER_NAME;
        }

        List<Map<String, Object>> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (itemIds != null) {
            for (int i = 0; i < itemIds.length; i++) {
                String idStr = itemIds[i];
                if (idStr == null || idStr.isBlank()) continue;

                int id = Integer.parseInt(idStr);

                int qty = 1;
                if (qtys != null && i < qtys.length && qtys[i] != null && !qtys[i].isBlank()) {
                    qty = Integer.parseInt(qtys[i].trim());
                }

                BigDecimal price = BigDecimal.ZERO;
                if (unitPrices != null && i < unitPrices.length
                        && unitPrices[i] != null && !unitPrices[i].isBlank()) {
                    price = new BigDecimal(unitPrices[i].trim());
                } else {
                    JsonObject it = itemIndex.get(id);
                    if (it != null && !it.isNull("unitPrice")) {
                        price = it.getJsonNumber("unitPrice").bigDecimalValue();
                    }
                }

                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
                total = total.add(lineTotal);

                String itemName = null;
                JsonObject it = itemIndex.get(id);
                if (it != null) itemName = it.getString("name", null);

                Map<String, Object> lm = new HashMap<>();
                lm.put("itemId", id);
                lm.put("itemName", itemName != null ? itemName : ("Item #" + id));
                lm.put("qty", Integer.valueOf(qty));
                lm.put("unitPrice", price);
                lm.put("lineTotal", lineTotal);
                lines.add(lm);
            }
        }

        Map<String, Object> m = new HashMap<>();
        m.put("billNo", "TMP-" + System.currentTimeMillis());
        m.put("customerId", Integer.valueOf(customerId));
        m.put("customerName", customerName != null ? customerName : "");
        m.put("createdAt", LocalDateTime.now().toString());
        m.put("items", lines);
        m.put("totalAmount", total);
        return m;
    }

    // Convert service JSON -> Map<String,Object> for JSPs (BigDecimal for money).
    private Map<String, Object> toViewModel(JsonObject b) {
        Map<String, Object> m = new HashMap<>();
        m.put("billNo", b.getString("billNo", ""));
        m.put("customerName", b.getString("customerName", ""));
        if (b.containsKey("customerId") && !b.isNull("customerId"))
            m.put("customerId", Integer.valueOf(b.getInt("customerId")));
        m.put("createdAt", b.getString("createdAt", ""));
        if (b.containsKey("totalAmount") && !b.isNull("totalAmount"))
            m.put("totalAmount", b.getJsonNumber("totalAmount").bigDecimalValue());

        List<Map<String, Object>> lines = new ArrayList<>();
        if (b.containsKey("items") && !b.isNull("items")) {
            JsonArray arr = b.getJsonArray("items");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject l = arr.getJsonObject(i);
                Map<String, Object> lm = new HashMap<>();
                if (l.containsKey("itemId") && !l.isNull("itemId"))
                    lm.put("itemId", Integer.valueOf(l.getInt("itemId")));
                lm.put("itemName", l.getString("itemName", ""));
                lm.put("qty", Integer.valueOf(l.getInt("qty", 1)));
                if (l.containsKey("unitPrice") && !l.isNull("unitPrice"))
                    lm.put("unitPrice", l.getJsonNumber("unitPrice").bigDecimalValue());
                if (l.containsKey("lineTotal") && !l.isNull("lineTotal"))
                    lm.put("lineTotal", l.getJsonNumber("lineTotal").bigDecimalValue());
                lines.add(lm);
            }
        }
        m.put("items", lines);
        return m;
    }

    /** Pick newest bill by createdAt for the given customer if possible; fallback to newest overall. */
    private static JsonObject pickNewestForCustomer(JsonArray arr, Integer customerId) {
        JsonObject newestMatch = null;
        String newestMatchKey = "";

        for (int i = 0; i < arr.size(); i++) {
            JsonObject b = arr.getJsonObject(i);
            // Prefer records that have matching customerId
            Integer bId = (b.containsKey("customerId") && !b.isNull("customerId"))
                    ? b.getInt("customerId") : null;

            if (customerId != null && bId != null && customerId.equals(bId)) {
                String c = b.getString("createdAt", "");
                String key = c.length() >= 19 ? c.substring(0, 19) : c; // yyyy-MM-ddTHH:mm:ss
                if (key.compareTo(newestMatchKey) > 0) {
                    newestMatch = b;
                    newestMatchKey = key;
                }
            }
        }

        if (newestMatch != null) return newestMatch;

        // Fallback: newest overall (if API didn't include customerId)
        return pickNewestByCreatedAt(arr);
    }

    /** Pick newest bill by createdAt (lexicographic on ISO string). */
    private static JsonObject pickNewestByCreatedAt(JsonArray arr) {
        JsonObject newest = null;
        String newestKey = "";

        for (int i = 0; i < arr.size(); i++) {
            JsonObject b = arr.getJsonObject(i);
            String c = b.getString("createdAt", "");
            String key = c.length() >= 19 ? c.substring(0, 19) : c;
            if (key.compareTo(newestKey) > 0) {
                newest = b;
                newestKey = key;
            }
        }
        return newest;
    }
}
