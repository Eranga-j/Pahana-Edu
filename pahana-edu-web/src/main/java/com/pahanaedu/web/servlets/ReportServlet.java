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

@WebServlet(name = "ReportServlet", urlPatterns = {"/admin/reports"})
public class ReportServlet extends HttpServlet {

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

        String from = trim(req.getParameter("from"));
        String to   = trim(req.getParameter("to"));

        // keep selected values in the form
        req.setAttribute("from", from == null ? "" : from);
        req.setAttribute("to",   to   == null ? "" : to);

        LocalDate fromD = (from == null || from.isEmpty()) ? null : LocalDate.parse(from);
        LocalDate toD   = (to   == null || to.isEmpty())   ? null : LocalDate.parse(to);

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        try {
            ApiClient api = new ApiClient(apiBase);

            // 1) if both dates are given, try server-side filtering first
            JsonArray arr = JsonValue.EMPTY_JSON_ARRAY;
            boolean triedFiltered = false;

            if (fromD != null && toD != null) {
                String q = "/bills?from=" + from + "T00:00:00&to=" + to + "T23:59:59";
                arr = fetchAsArray(api, q);
                triedFiltered = true;
            }

            // 2) fallback: fetch all and filter locally if the filtered call returned nothing
            if (!triedFiltered || arr.isEmpty()) {
                JsonArray all = fetchAsArray(api, "/bills");
                if (fromD != null || toD != null) {
                    // local inclusive filter by yyyy-MM-dd
                    JsonArrayBuilder kept = Json.createArrayBuilder();
                    for (JsonValue v : all) {
                        if (!(v instanceof JsonObject)) continue;
                        JsonObject b = (JsonObject) v;
                        String createdAt = b.containsKey("createdAt") && !b.isNull("createdAt")
                                ? b.getString("createdAt") : "";
                        LocalDate billDate = parseDate(createdAt);
                        boolean ok = true;
                        if (fromD != null && billDate != null && billDate.isBefore(fromD)) ok = false;
                        if (toD   != null && billDate != null && billDate.isAfter(toD))   ok = false;
                        if (ok) kept.add(b);
                    }
                    arr = kept.build();
                } else {
                    arr = all; // no dates -> show all
                }
            }

            // 3) build rows for the JSP
            for (int i = 0; i < arr.size(); i++) {
                JsonObject b = arr.getJsonObject(i);

                Map<String, Object> m = new HashMap<>();
                m.put("billNo", b.getString("billNo", ""));

                String custName = b.getString("customerName", "");
                m.put("customerName", custName);
                if (b.containsKey("customerId") && !b.isNull("customerId")) {
                    m.put("customerId", Integer.valueOf(b.getInt("customerId")));
                }

                String createdAt = b.containsKey("createdAt") && !b.isNull("createdAt")
                        ? b.getString("createdAt") : "";
                m.put("createdAt", createdAt);

                int lineCount = (b.containsKey("items") && !b.isNull("items"))
                        ? b.getJsonArray("items").size() : 0;
                m.put("lineCount", lineCount);

                BigDecimal total = b.isNull("totalAmount")
                        ? BigDecimal.ZERO
                        : b.getJsonNumber("totalAmount").bigDecimalValue();
                m.put("totalAmount", total);

                rows.add(m);
                sum = sum.add(total);
            }

        } catch (Exception e) {
            // surface any backend error to the page
            req.setAttribute("error", e.getMessage());
        }

        req.setAttribute("bills", rows);
        req.setAttribute("sumAmount", sum);
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    /* ---------- helpers ---------- */

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static LocalDate parseDate(String createdAt) {
        try {
            if (createdAt != null && createdAt.length() >= 10) {
                return LocalDate.parse(createdAt.substring(0, 10)); // yyyy-MM-dd
            }
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * Accepts: a bare JSON array OR an object like {content:[...]} or {data:[...]}.
     * Returns an empty array if nothing usable is found.
     */
    private static JsonArray fetchAsArray(ApiClient api, String path) throws IOException {
        JsonStructure js = api.getJson(path);
        if (js instanceof JsonArray) return js.asJsonArray();
        if (js instanceof JsonObject) {
            JsonObject o = js.asJsonObject();
            if (o.containsKey("content") && o.get("content") instanceof JsonArray) {
                return o.getJsonArray("content");
            }
            if (o.containsKey("data") && o.get("data") instanceof JsonArray) {
                return o.getJsonArray("data");
            }
        }
        return JsonValue.EMPTY_JSON_ARRAY;
    }
}
