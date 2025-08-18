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

        // Read form values early so we can repopulate on error
        String accountNumber = safe(req.getParameter("accountNumber"));
        String name          = safe(req.getParameter("name"));
        String address       = safe(req.getParameter("address"));
        String phone         = safe(req.getParameter("phone"));

        ApiClient api = new ApiClient(apiBase);

        JsonObject payload = Json.createObjectBuilder()
                .add("accountNumber", accountNumber)
                .add("name",          name)
                .add("address",       address)
                .add("phone",         phone)
                .build();

        try {
            switch (action) {
                case "create": {
                    // --- SERVER-SIDE DUPLICATE CHECK (create)
                    if (!phone.isEmpty() && existsByPhone(api, phone)) {
                        forwardWithError(req, resp, "/customers/create.jsp",
                                "This phone number is already registered.",
                                accountNumber, name, address, phone);
                        return;
                    }
                    // attempt create
                    try {
                        api.sendJsonForJson("POST", "/customers", payload.toString());
                    } catch (IOException e) {
                        // final safety: DB unique index duplicate
                        if (isDuplicateError(e)) {
                            forwardWithError(req, resp, "/customers/create.jsp",
                                    "This phone number is already registered.",
                                    accountNumber, name, address, phone);
                            return;
                        }
                        throw e;
                    }
                    resp.sendRedirect(req.getContextPath() + "/customers?created=1");
                    return;
                }

                case "update": {
                    if (idStr.isEmpty()) {
                        // treat as create if id missing
                        if (!phone.isEmpty() && existsByPhone(api, phone)) {
                            forwardWithError(req, resp, "/customers/create.jsp",
                                    "This phone number is already registered.",
                                    accountNumber, name, address, phone);
                            return;
                        }
                        try {
                            api.sendJsonForJson("POST", "/customers", payload.toString());
                        } catch (IOException e) {
                            if (isDuplicateError(e)) {
                                forwardWithError(req, resp, "/customers/create.jsp",
                                        "This phone number is already registered.",
                                        accountNumber, name, address, phone);
                                return;
                            }
                            throw e;
                        }
                        resp.sendRedirect(req.getContextPath() + "/customers?created=1");
                        return;
                    } else {
                        // --- SERVER-SIDE DUPLICATE CHECK (update; ignore self)
                        int id = Integer.parseInt(idStr);
                        if (!phone.isEmpty() && existsByPhoneExceptId(api, phone, id)) {
                            // keep id so edit page knows which record
                            req.setAttribute("id", idStr);
                            forwardWithError(req, resp, "/customers/edit.jsp",
                                    "This phone number is already registered.",
                                    accountNumber, name, address, phone);
                            return;
                        }
                        try {
                            api.sendJsonForJson("PUT", "/customers/" + idStr, payload.toString());
                        } catch (IOException e) {
                            if (isDuplicateError(e)) {
                                req.setAttribute("id", idStr);
                                forwardWithError(req, resp, "/customers/edit.jsp",
                                        "This user is already registered.",
                                        accountNumber, name, address, phone);
                                return;
                            }
                            throw e;
                        }
                        resp.sendRedirect(req.getContextPath() + "/customers?updated=1");
                        return;
                    }
                }

                case "delete": {
                    if (!idStr.isEmpty()) {
                        deleteNoBody("/customers/" + idStr);
                    }
                    resp.sendRedirect(req.getContextPath() + "/customers?deleted=1");
                    return;
                }

                default: {
                    // No recognized action; go back to list
                    resp.sendRedirect(req.getContextPath() + "/customers");
                    return;
                }
            }
        } catch (Exception ex) {
            // unexpected error -> show generic message on create page
            forwardWithError(req, resp, "/customers/create.jsp",
                    "Something went wrong while saving the customer.",
                    accountNumber, name, address, phone);
        }
    }

    /** Existence check by phone (any record). */
    private boolean existsByPhone(ApiClient api, String phone) {
        try {
            String q = "/customers?phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8);
            JsonStructure js = api.getJson(q);
            if (js == null) return false;
            switch (js.getValueType()) {
                case ARRAY:  return !js.asJsonArray().isEmpty();
                case OBJECT: return true;
                default:     return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Existence check by phone excluding a specific id (for updates). */
    private boolean existsByPhoneExceptId(ApiClient api, String phone, int selfId) {
        try {
            String q = "/customers?phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8);
            JsonStructure js = api.getJson(q);
            if (js == null) return false;

            if (js.getValueType() == JsonStructure.ValueType.ARRAY) {
                JsonArray arr = js.asJsonArray();
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject o = arr.getJsonObject(i);
                    if (o.containsKey("id") && !o.isNull("id")) {
                        int id = o.getInt("id");
                        if (id != selfId) return true;
                    } else {
                        // if API doesn’t return id, assume conflict
                        return true;
                    }
                }
                return false;
            } else if (js.getValueType() == JsonStructure.ValueType.OBJECT) {
                JsonObject o = js.asJsonObject();
                if (o.containsKey("id") && !o.isNull("id")) {
                    return o.getInt("id") != selfId;
                }
                return true; // no id to compare; treat as conflict
            }
        } catch (Exception ignored) {}
        return false;
        }

    /** Forward back to a form with an error and the entered values. */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
                                  String jspPath, String errorMsg,
                                  String accountNumber, String name, String address, String phone)
            throws ServletException, IOException {
        req.setAttribute("error", errorMsg);
        req.setAttribute("form_accountNumber", accountNumber);
        req.setAttribute("form_name",          name);
        req.setAttribute("form_address",       address);
        req.setAttribute("form_phone",         phone);
        req.getRequestDispatcher(jspPath).forward(req, resp);
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
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private static boolean isDuplicateError(IOException e) {
        String msg = String.valueOf(e.getMessage()).toLowerCase();
        return msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_") || msg.contains("constraint");
    }
}
