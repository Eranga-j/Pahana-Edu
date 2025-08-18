package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    private String apiBase;

    @Override
    public void init() {
        this.apiBase = getServletContext().getInitParameter("apiBase");
        if (this.apiBase == null) this.apiBase = "http://localhost:8080/pahana-edu-service/api";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ApiClient api = new ApiClient(apiBase);
        JsonStructure res = api.getJson("/customers");
        JsonArray arr = res.asJsonArray();

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.getJsonObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.valueOf(o.getInt("id")));
            m.put("accountNumber", o.getString("accountNumber", ""));
            m.put("name", o.getString("name", ""));
            m.put("address", (o.containsKey("address") && !o.isNull("address")) ? o.getString("address") : "");
            m.put("phone",   (o.containsKey("phone")   && !o.isNull("phone"))   ? o.getString("phone")   : "");
            list.add(m);
        }

        req.setAttribute("customers", list);
        req.getRequestDispatcher("/customers/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = safe(req.getParameter("action"));
        String idStr  = safe(req.getParameter("id"));
        ApiClient api = new ApiClient(apiBase);

        JsonObject payload = Json.createObjectBuilder()
                .add("accountNumber", safe(req.getParameter("accountNumber")))
                .add("name",          safe(req.getParameter("name")))
                .add("address",       safe(req.getParameter("address")))
                .add("phone",         safe(req.getParameter("phone")))
                .build();

        try {
            switch (action) {
                case "create": {
                    try { api.sendJsonForJson("POST", "/customers", payload.toString()); } catch (IOException ignore) {}
                    break;
                }
                case "update": {
                    if (idStr.isEmpty()) {
                        try { api.sendJsonForJson("POST", "/customers", payload.toString()); } catch (IOException ignore) {}
                    } else {
                        try { api.sendJsonForJson("PUT", "/customers/" + idStr, payload.toString()); } catch (IOException ignore) {}
                    }
                    break;
                }
                case "delete": {
                    if (!idStr.isEmpty()) {
                        // *** IMPORTANT: DELETE with NO BODY ***
                        deleteNoBody("/customers/" + idStr);
                    }
                    break;
                }
                default:
                    // no-op
            }
        } finally {
            resp.sendRedirect(req.getContextPath() + "/customers");
        }
    }

    /** Issue a DELETE with no request body (works across stacks). */
    private void deleteNoBody(String path) throws IOException {
        String urlStr = apiBase + path;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(false);               // <-- crucial: no body
        conn.connect();
        int code = conn.getResponseCode();     // 200/204/202 are all fine
        conn.disconnect();
        // (Optional) you could log unexpected codes if needed
        // if (code >= 400) System.err.println("DELETE failed " + code + " for " + urlStr);
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
