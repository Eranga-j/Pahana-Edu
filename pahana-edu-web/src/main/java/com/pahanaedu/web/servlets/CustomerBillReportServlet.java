package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@WebServlet(name = "CustomerBillReportServlet", urlPatterns = {"/admin/customer-report"})
public class CustomerBillReportServlet extends HttpServlet {

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

        // --- Load customers for dropdown
        List<Map<String, Object>> customers = new ArrayList<>();
        try {
            JsonArray custArr = api.getJson("/customers").asJsonArray();
            for (int i = 0; i < custArr.size(); i++) {
                JsonObject o = custArr.getJsonObject(i);
                Map<String, Object> m = new HashMap<>();
                m.put("id", Integer.valueOf(o.getInt("id")));
                m.put("accountNumber", o.getString("accountNumber", ""));
                m.put("name", o.getString("name", ""));
                customers.add(m);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Failed to load customers: " + e.getMessage());
        }
        req.setAttribute("customers", customers);

        // --- Inputs
        String custIdStr = trim(req.getParameter("customerId"));
        String from = trim(req.getParameter("from"));
        String to   = trim(req.getParameter("to"));

        req.setAttribute("selectedCustomerId", custIdStr == null ? "" : custIdStr);
        req.setAttribute("from", from == null ? "" : from);
        req.setAttribute("to",   to   == null ? "" : to);

        Integer custId = isInt(custIdStr) ? Integer.valueOf(custIdStr) : null;
        LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate toDate   = (to   != null && !to.isEmpty())   ? LocalDate.parse(to)   : null;

        // If only one date is given, treat as a single-day range
        if (fromDate != null && toDate == null) toDate = fromDate;
        if (toDate   != null && fromDate == null) fromDate = toDate;

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        try {
            // ---- Try several likely endpoints; pick the first non-empty
            List<String> candidates = new ArrayList<>();
            if (custId != null && fromDate != null && toDate != null) {
                // With customer + range
                candidates.add("/bills?customerId=" + custId
                        + "&from=" + fromDate + "T00:00:00&to=" + toDate + "T23:59:59");
            }
            if (custId != null) {
                candidates.add("/bills?customerId=" + custId);
            }
            if (fromDate != null && toDate != null) {
                candidates.add("/bills?from=" + fromDate + "T00:00:00&to=" + toDate + "T23:59:59");
            }
            // Some services use alternate param names — try a couple of fallbacks:
            if (custId != null && fromDate != null && toDate != null) {
                candidates.add("/bills?customer_id=" + custId
                        + "&start=" + fromDate + "T00:00:00&end=" + toDate + "T23:59:59");
            }
            // Plain list
            candidates.add("/bills");

            JsonArray bills = Json.createArrayBuilder().build();
            for (String q : candidates) {
                try {
                    JsonStructure js = api.getJson(q);
                    if (js instanceof JsonArray arr) {
                        if (!arr.isEmpty()) {
                            bills = arr;
                            break;
                        } else {
                            bills = arr; // keep last empty array if none are non-empty
                        }
                    }
                } catch (Exception ignored) {
                    // try next candidate
                }
            }

            // ---- If the server didn't filter, do it locally
            List<JsonObject> filtered = new ArrayList<>();
            for (int i = 0; i < bills.size(); i++) {
                JsonObject b = bills.getJsonObject(i);

                // Customer filter
                Integer bCustId = (b.containsKey("customerId") && !b.isNull("customerId"))
                        ? Integer.valueOf(b.getInt("customerId"))
                        : null;
                if (custId != null && (bCustId == null || !bCustId.equals(custId))) continue;

                // Date filter (by DATE only)
                String createdAt = b.containsKey("createdAt") && !b.isNull("createdAt")
                        ? b.getString("createdAt")
                        : null;
                if (fromDate != null && toDate != null) {
                    LocalDate billDate = extractDate(createdAt);
                    if (billDate == null || billDate.isBefore(fromDate) || billDate.isAfter(toDate)) {
                        continue;
                    }
                }

                filtered.add(b);
            }

            // ---- Sort newest first by createdAt
            filtered.sort((a, b) -> safeCreatedAt(b).compareTo(safeCreatedAt(a)));

            // ---- Convert to JSP model
            for (JsonObject b : filtered) {
                String createdAt = b.containsKey("createdAt") && !b.isNull("createdAt")
                        ? b.getString("createdAt")
                        : "";

                Map<String, Object> row = new HashMap<>();
                row.put("billNo", b.getString("billNo", ""));
                row.put("customerName", b.getString("customerName", ""));
                row.put("createdAt", createdAt);

                BigDecimal total = b.isNull("totalAmount")
                        ? BigDecimal.ZERO
                        : b.getJsonNumber("totalAmount").bigDecimalValue();
                row.put("totalAmount", total);

                // Items list (if present)
                List<Map<String, Object>> items = new ArrayList<>();
                if (b.containsKey("items") && !b.isNull("items")) {
                    JsonArray arr = b.getJsonArray("items");
                    for (int j = 0; j < arr.size(); j++) {
                        JsonObject l = arr.getJsonObject(j);
                        Map<String, Object> lm = new HashMap<>();
                        lm.put("itemName", l.getString("itemName", ""));
                        lm.put("qty", Integer.valueOf(l.getInt("qty", 1)));
                        if (l.containsKey("unitPrice") && !l.isNull("unitPrice"))
                            lm.put("unitPrice", l.getJsonNumber("unitPrice").bigDecimalValue());
                        if (l.containsKey("lineTotal") && !l.isNull("lineTotal"))
                            lm.put("lineTotal", l.getJsonNumber("lineTotal").bigDecimalValue());
                        if (l.containsKey("itemId") && !l.isNull("itemId"))
                            lm.put("itemId", Integer.valueOf(l.getInt("itemId")));
                        items.add(lm);
                    }
                }
                row.put("items", items);

                rows.add(row);
                sum = sum.add(total);
            }

        } catch (Exception e) {
            req.setAttribute("error", "Failed to load bills: " + e.getMessage());
        }

        req.setAttribute("rows", rows);
        req.setAttribute("sumAmount", sum);
        req.getRequestDispatcher("/admin/customer-report.jsp").forward(req, resp);
    }

    /* ================= helpers ================= */

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean isInt(String s) { return s != null && s.matches("\\d+"); }

    /** Compare by date only; accept "2025-08-17T23:20:06.147" or "2025-08-17 23:20". */
    private static LocalDate extractDate(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) return null;
        if (createdAt.length() >= 10) {
            try {
                return LocalDate.parse(createdAt.substring(0, 10));
            } catch (Exception ignored) {}
        }
        return null;
    }

    /** Safe sort key for createdAt (YYYY-MM-DD prefix; empty sorts to oldest). */
    private static String safeCreatedAt(JsonObject o) {
        String s = (o.containsKey("createdAt") && !o.isNull("createdAt"))
                ? o.getString("createdAt", "")
                : "";
        if (s.length() >= 19) return s.substring(0, 19);
        if (s.length() >= 10) return s.substring(0, 10);
        return ""; // oldest
    }
}
