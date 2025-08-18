package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet(name = "ItemServlet", urlPatterns = {"/items"})
public class ItemServlet extends HttpServlet {

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
        JsonArray arr = api.getJson("/items").asJsonArray();

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.getJsonObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.valueOf(o.getInt("id")));
            m.put("sku", o.getString("sku", ""));
            m.put("name", o.getString("name", ""));
            BigDecimal price = o.isNull("unitPrice")
                    ? BigDecimal.ZERO
                    : o.getJsonNumber("unitPrice").bigDecimalValue();
            m.put("unitPrice", price);
            list.add(m);
        }

        req.setAttribute("items", list);
        req.getRequestDispatcher("/items/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Admin-only guard
        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (!"ADMIN".equals(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admin can modify items.");
            return;
        }

        String actionRaw = req.getParameter("action");
        String action = actionRaw == null ? "" : actionRaw.trim().toLowerCase(Locale.ROOT);

        // Be tolerant to different delete triggers
        boolean wantsDelete =
                "delete".equals(action) ||
                req.getParameter("delete") != null ||
                "delete".equalsIgnoreCase(req.getParameter("_method")) ||
                req.getParameter("deleteId") != null;

        ApiClient api = new ApiClient(apiBase);

        try {
            if (wantsDelete) {
                String id = firstNonBlank(req.getParameter("id"), req.getParameter("deleteId"));
                if (!isInt(id)) {
                    fail(req, resp, "Invalid item id for deletion.");
                    return;
                }
                // No body, no Content-Type — matches ApiClient.delete()
                api.delete("/items/" + id.trim());
                resp.sendRedirect(req.getContextPath() + "/items");
                return;
            }

            if ("create".equals(action)) {
                BigDecimal unitPrice = parsePrice(req.getParameter("unitPrice"));
                JsonObject json = Json.createObjectBuilder()
                        .add("sku", nn(req.getParameter("sku")))
                        .add("name", nn(req.getParameter("name")))
                        .add("unitPrice", unitPrice)
                        .build();
                api.sendJsonNoBody("POST", "/items", json.toString());
                resp.sendRedirect(req.getContextPath() + "/items");
                return;
            }

            if ("update".equals(action)) {
                String id = req.getParameter("id");
                if (!isInt(id)) {
                    fail(req, resp, "Invalid item id for update.");
                    return;
                }
                BigDecimal unitPrice = parsePrice(req.getParameter("unitPrice"));
                JsonObject json = Json.createObjectBuilder()
                        .add("sku", nn(req.getParameter("sku")))
                        .add("name", nn(req.getParameter("name")))
                        .add("unitPrice", unitPrice)
                        .build();
                api.sendJsonNoBody("PUT", "/items/" + id.trim(), json.toString());
                resp.sendRedirect(req.getContextPath() + "/items");
                return;
            }

            // Unknown action -> back to list
            resp.sendRedirect(req.getContextPath() + "/items");

        } catch (Exception ex) {
            // e.g., FK constraint if item used in a bill, or backend message
            fail(req, resp, ex.getMessage());
        }
    }

    /* ---------------- helpers ---------------- */

    private static BigDecimal parsePrice(String s) {
        if (s == null) return BigDecimal.ZERO;
        s = s.trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String nn(String s) { return s == null ? "" : s; }

    private static boolean isInt(String s) {
        return s != null && s.trim().matches("\\d+");
    }

    private static String firstNonBlank(String... v) {
        for (String s : v) if (s != null && !s.isBlank()) return s;
        return null;
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws ServletException, IOException {
        req.setAttribute("error", (msg == null || msg.isBlank()) ? "Operation failed." : msg);
        doGet(req, resp); // show list with error banner
    }
}
