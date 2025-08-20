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

    private static final int    GUEST_CUSTOMER_ID   = 1;
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

        JsonArray custArr = api.getJson("/customers").asJsonArray();
        List<Map<String, Object>> customers = new ArrayList<>();
        for (int i = 0; i < custArr.size(); i++) {
            JsonObject o = custArr.getJsonObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.valueOf(o.getInt("id")));
            m.put("accountNumber", o.getString("accountNumber", ""));
            m.put("name", o.getString("name", ""));
            customers.add(m);
        }

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

        String customerIdStr = req.getParameter("customerId");
        String[] itemIds     = req.getParameterValues("itemId");
        String[] qtys        = req.getParameterValues("qty");
        String[] unitPrices  = req.getParameterValues("unitPrice");

        int customerId = (customerIdStr == null || customerIdStr.isBlank())
                ? GUEST_CUSTOMER_ID : Integer.parseInt(customerIdStr);

        HttpSession session = req.getSession(false);
        int createdBy = 1;
        if (session != null) {
            Object u = session.getAttribute("userId");
            if (u instanceof Integer) createdBy = (Integer) u;
            else if (u instanceof String && !((String) u).isBlank()) {
                try { createdBy = Integer.parseInt((String) u); } catch (NumberFormatException ignored) {}
            }
        }

        JsonArrayBuilder lines = Json.createArrayBuilder();
        BigDecimal subtotal = BigDecimal.ZERO;
        int chosen = 0;

        if (itemIds != null) {
            for (int i = 0; i < itemIds.length; i++) {
                String idStr = itemIds[i];
                if (idStr == null || idStr.isBlank()) continue;
                int itemId = Integer.parseInt(idStr);

                int qty = 1;
                if (qtys != null && i < qtys.length && qtys[i] != null && !qtys[i].isBlank()) {
                    try { qty = Math.max(1, Integer.parseInt(qtys[i].trim())); } catch (NumberFormatException ignored) {}
                }

                BigDecimal price = BigDecimal.ZERO;
                if (unitPrices != null && i < unitPrices.length && unitPrices[i] != null && !unitPrices[i].isBlank()) {
                    try {
                        price = new BigDecimal(unitPrices[i].trim());
                        if (price.signum() < 0) price = BigDecimal.ZERO;
                    } catch (NumberFormatException ignored) {}
                }

                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
                subtotal = subtotal.add(lineTotal);
                chosen++;

                // NOTE: service expects itemId, qty, unitPrice; sending lineTotal is harmless but optional
                lines.add(Json.createObjectBuilder()
                        .add("itemId", itemId)
                        .add("qty", qty)
                        .add("unitPrice", price)
                        .add("lineTotal", lineTotal));
            }
        }

        if (chosen == 0) {
            req.setAttribute("error", "Please select at least one item.");
            doGet(req, resp);
            return;
        }

        // Service calculates totals; we keep local totals only for UI
        BigDecimal tax   = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(tax);

        JsonObject payload = Json.createObjectBuilder()
                .add("customerId",  customerId)
                .add("createdBy",   createdBy)
                .add("lines",       lines)  // <-- CHANGED from "items" to "lines"
                .build();                   // <-- REMOVED totalAmount from payload

        JsonObject createdObj = null;
        try {
            JsonStructure js = api.sendJsonForJson("POST", "/bills", payload.toString());
            if (js != null && js.getValueType() == JsonValue.ValueType.OBJECT) {
                createdObj = js.asJsonObject();
            }
        } catch (IOException e) {
            req.setAttribute("error", "Could not save the bill. Check the service is running and required fields are valid.");
            doGet(req, resp);
            return;
        }

        Map<String, Object> billVM = null;
        if (createdObj != null
                && createdObj.containsKey("items")
                && !createdObj.isNull("items")
                && createdObj.getJsonArray("items").size() > 0) {
            billVM = toViewModel(createdObj);
        } else {
            try {
                JsonStructure js = api.getJson("/bills?customerId=" + customerId);
                if (js instanceof JsonArray arr) {
                    JsonObject newest = pickNewestForCustomer(arr, customerId);
                    if (newest != null) billVM = toViewModel(newest);
                }
            } catch (Exception ignored) {}
        }

        if (billVM == null) {
            req.setAttribute("error", "Bill was not created by the service.");
            doGet(req, resp);
            return;
        }

        // -------- SUCCESS: forward to invoice page --------
        req.setAttribute("bill", billVM);
        req.getSession(true).setAttribute("billForPrint", billVM); // NEW: save for print/pdf
        req.getRequestDispatcher("/bills/view.jsp").forward(req, resp);
    }

    private Map<String, Object> buildLocalViewModel(ApiClient api,
                                                    int customerId,
                                                    String[] itemIds,
                                                    String[] qtys,
                                                    String[] unitPrices) throws IOException {
        Map<Integer, JsonObject> itemIndex = new HashMap<>();
        JsonArray allItems = api.getJson("/items").asJsonArray();
        for (int i = 0; i < allItems.size(); i++) {
            JsonObject it = allItems.getJsonObject(i);
            itemIndex.put(it.getInt("id"), it);
        }

        String customerName = null;
        try {
            JsonObject c = api.getJson("/customers/" + customerId).asJsonObject();
            customerName = c.getString("name", null);
        } catch (Exception ignored) {}

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

    private static JsonObject pickNewestForCustomer(JsonArray arr, Integer customerId) {
        JsonObject newestMatch = null;
        String newestMatchKey = "";

        for (int i = 0; i < arr.size(); i++) {
            JsonObject b = arr.getJsonObject(i);
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
        return pickNewestByCreatedAt(arr);
    }

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
