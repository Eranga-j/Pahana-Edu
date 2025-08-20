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
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    private static final String ERR_PHONE   = "Mobile number already registered";
    private static final String ERR_ACCOUNT = "Account number already registered";

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
                    // server-side duplicate checks
                    if (accountTaken(api, accountNumber, null)) {
                        setFieldErrors(req, true, false);
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                        return;
                    }
                    if (phoneTaken(api, phone, null)) {
                        setFieldErrors(req, false, true);
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                        return;
                    }

                    try {
                        api.sendJsonForJson("POST", "/customers", payload.toString());
                    } catch (IOException e) {
                        // On DB unique violation, decide which field
                        if (accountTaken(api, accountNumber, null)) {
                            setFieldErrors(req, true, false);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                            return;
                        }
                        if (phoneTaken(api, phone, null)) {
                            setFieldErrors(req, false, true);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                            return;
                        }
                        throw e;
                    }
                    resp.sendRedirect(req.getContextPath() + "/customers?created=1");
                    return;
                }

                case "update": {
                    Integer id = idStr.isEmpty() ? null : Integer.valueOf(idStr);

                    // treat missing id like create
                    if (id == null) {
                        if (accountTaken(api, accountNumber, null)) {
                            setFieldErrors(req, true, false);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                            return;
                        }
                        if (phoneTaken(api, phone, null)) {
                            setFieldErrors(req, false, true);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                            return;
                        }
                        api.sendJsonForJson("POST", "/customers", payload.toString());
                        resp.sendRedirect(req.getContextPath() + "/customers?created=1");
                        return;
                    }

                    // update (ignore self)
                    if (accountTaken(api, accountNumber, id)) {
                        req.setAttribute("id", idStr);
                        setFieldErrors(req, true, false);
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
                        return;
                    }
                    if (phoneTaken(api, phone, id)) {
                        req.setAttribute("id", idStr);
                        setFieldErrors(req, false, true);
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
                        return;
                    }

                    try {
                        api.sendJsonForJson("PUT", "/customers/" + idStr, payload.toString());
                    } catch (IOException e) {
                        req.setAttribute("id", idStr);
                        if (accountTaken(api, accountNumber, id)) {
                            setFieldErrors(req, true, false);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
                            return;
                        }
                        if (phoneTaken(api, phone, id)) {
                            setFieldErrors(req, false, true);
                            keepForm(req, accountNumber, name, address, phone);
                            req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
                            return;
                        }
                        throw e;
                    }
                    resp.sendRedirect(req.getContextPath() + "/customers?updated=1");
                    return;
                }

                case "delete": {
                    if (!idStr.isEmpty()) deleteNoBody("/customers/" + idStr);
                    resp.sendRedirect(req.getContextPath() + "/customers?deleted=1");
                    return;
                }

                default: {
                    resp.sendRedirect(req.getContextPath() + "/customers");
                    return;
                }
            }
        } catch (Exception ex) {
            // unexpected error -> generic message on create page
            req.setAttribute("error", "Something went wrong while saving the customer.");
            keepForm(req, accountNumber, name, address, phone);
            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
        }
    }

    /* ================= Duplicate checks ================= */

    /** true if another customer already uses this phone; ignore selfId when updating. */
    private boolean phoneTaken(ApiClient api, String phone, Integer selfId) {
        String target = normalizePhone(phone);

        // Try filtered endpoint; if backend ignores it, we still scan the results
        try {
            JsonStructure js = api.getJson("/customers?phone=" + url(phone));
            if (js != null) {
                if (js.getValueType() == JsonStructure.ValueType.ARRAY) {
                    if (matchPhone(js.asJsonArray(), target, selfId)) return true;
                } else if (js.getValueType() == JsonStructure.ValueType.OBJECT) {
                    if (matchPhone(js.asJsonObject(), target, selfId)) return true;
                }
            }
        } catch (Exception ignored) {}

        // Fallback: fetch all and scan
        try {
            JsonArray arr = api.getJson("/customers").asJsonArray();
            return matchPhone(arr, target, selfId);
        } catch (Exception ignored) { return false; }
    }

    /** true if another customer already uses this account number; ignore selfId on update. */
    private boolean accountTaken(ApiClient api, String account, Integer selfId) {
        String target = normalizeAccount(account);

        try {
            JsonStructure js = api.getJson("/customers?accountNumber=" + url(account));
            if (js != null) {
                if (js.getValueType() == JsonStructure.ValueType.ARRAY) {
                    if (matchAccount(js.asJsonArray(), target, selfId)) return true;
                } else if (js.getValueType() == JsonStructure.ValueType.OBJECT) {
                    if (matchAccount(js.asJsonObject(), target, selfId)) return true;
                }
            }
        } catch (Exception ignored) {}

        try {
            JsonArray arr = api.getJson("/customers").asJsonArray();
            return matchAccount(arr, target, selfId);
        } catch (Exception ignored) { return false; }
    }

    private static boolean matchPhone(JsonArray arr, String target, Integer selfId) {
        for (int i = 0; i < arr.size(); i++) if (matchPhone(arr.getJsonObject(i), target, selfId)) return true;
        return false;
    }
    private static boolean matchPhone(JsonObject o, String target, Integer selfId) {
        String ph = o.containsKey("phone") && !o.isNull("phone") ? o.getString("phone") : "";
        String norm = normalizePhone(ph);
        Integer id = (o.containsKey("id") && !o.isNull("id")) ? Integer.valueOf(o.getInt("id")) : null;
        if (selfId != null && id != null && selfId.equals(id)) return false;
        return !target.isEmpty() && target.equals(norm);
    }

    private static boolean matchAccount(JsonArray arr, String target, Integer selfId) {
        for (int i = 0; i < arr.size(); i++) if (matchAccount(arr.getJsonObject(i), target, selfId)) return true;
        return false;
    }
    private static boolean matchAccount(JsonObject o, String target, Integer selfId) {
        String acc = o.containsKey("accountNumber") && !o.isNull("accountNumber") ? o.getString("accountNumber") : "";
        String norm = normalizeAccount(acc);
        Integer id = (o.containsKey("id") && !o.isNull("id")) ? Integer.valueOf(o.getInt("id")) : null;
        if (selfId != null && id != null && selfId.equals(id)) return false;
        return !target.isEmpty() && target.equals(norm);
    }

    private static String normalizePhone(String s) {
        return (s == null) ? "" : s.replaceAll("\\D+", ""); // digits only
    }
    private static String normalizeAccount(String s) {
        return (s == null) ? "" : s.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
    private static String url(String s) {
        try { return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }

    /* ================= Helpers ================= */

    private static void keepForm(HttpServletRequest req, String acc, String nm, String addr, String ph) {
        req.setAttribute("form_accountNumber", acc);
        req.setAttribute("form_name",          nm);
        req.setAttribute("form_address",       addr);
        req.setAttribute("form_phone",         ph);
    }

    /** Set per-field error attributes for JSP (bubbled under inputs). */
    private static void setFieldErrors(HttpServletRequest req, boolean accountError, boolean phoneError) {
        if (accountError) req.setAttribute("e_account", ERR_ACCOUNT);
        if (phoneError)   req.setAttribute("e_phone",   ERR_PHONE);
    }

    private void deleteNoBody(String path) throws IOException {
        String urlStr = apiBase + path;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(false);
        conn.connect();
        conn.getResponseCode();
        conn.disconnect();
    }

    private static String safe(String v) { return v == null ? "" : v.trim(); }

    @SuppressWarnings("unused")
    private static boolean isDuplicateError(IOException e) {
        String msg = String.valueOf(e.getMessage()).toLowerCase();
        return msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_") || msg.contains("constraint");
    }
}
