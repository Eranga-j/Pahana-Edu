package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        JsonArray arr = api.getJson("/customers").asJsonArray();

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
                    // Fast path: pre-check duplicates
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

                    ApiResponse r = sendJsonWithStatus("POST", "/customers", payload.toString());
                    if (r.status >= 200 && r.status < 300) {
                        resp.sendRedirect(req.getContextPath() + "/customers?created=1");
                        return;
                    }
                    if (r.status == 409) { // duplicate from service
                        String code = parseErrorCode(r.body);
                        if ("DUPLICATE_ACCOUNT".equals(code) || accountTaken(api, accountNumber, null)) {
                            setFieldErrors(req, true, false);
                        } else if ("DUPLICATE_PHONE".equals(code) || phoneTaken(api, phone, null)) {
                            setFieldErrors(req, false, true);
                        } else {
                            req.setAttribute("error", "Duplicate value detected.");
                        }
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                        return;
                    }

                    // Unexpected error
                    req.setAttribute("error", "Could not save customer. Please try again.");
                    keepForm(req, accountNumber, name, address, phone);
                    req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
                    return;
                }

                case "update": {
                    Integer id = idStr.isEmpty() ? null : Integer.valueOf(idStr);

                    if (id == null) { // treat like create
                        resp.sendRedirect(req.getContextPath() + "/customers");
                        return;
                    }

                    // Pre-check duplicates (ignoring self)
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

                    ApiResponse r = sendJsonWithStatus("PUT", "/customers/" + idStr, payload.toString());
                    if (r.status >= 200 && r.status < 300) {
                        resp.sendRedirect(req.getContextPath() + "/customers?updated=1");
                        return;
                    }
                    if (r.status == 409) {
                        req.setAttribute("id", idStr);
                        String code = parseErrorCode(r.body);
                        if ("DUPLICATE_ACCOUNT".equals(code) || accountTaken(api, accountNumber, id)) {
                            setFieldErrors(req, true, false);
                        } else if ("DUPLICATE_PHONE".equals(code) || phoneTaken(api, phone, id)) {
                            setFieldErrors(req, false, true);
                        } else {
                            req.setAttribute("error", "Duplicate value detected.");
                        }
                        keepForm(req, accountNumber, name, address, phone);
                        req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
                        return;
                    }

                    req.setAttribute("id", idStr);
                    req.setAttribute("error", "Could not update customer. Please try again.");
                    keepForm(req, accountNumber, name, address, phone);
                    req.getRequestDispatcher("/customers/edit.jsp").forward(req, resp);
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
            getServletContext().log("Customer save failed", ex);
            req.setAttribute("error", "Something went wrong while saving the customer.");
            keepForm(req, accountNumber, name, address, phone);
            req.getRequestDispatcher("/customers/create.jsp").forward(req, resp);
        }
    }

    /* ================= Duplicate checks ================= */

    /** true if another customer already uses this phone; ignore selfId when updating. */
    private boolean phoneTaken(ApiClient api, String phone, Integer selfId) {
        String target = normalizePhone(phone);

        try {
            JsonStructure js = api.getJson("/customers?phone=" + url(phone));
            if (js != null) {
                if (js.getValueType() == JsonValue.ValueType.ARRAY) {
                    if (matchPhone(js.asJsonArray(), target, selfId)) return true;
                } else if (js.getValueType() == JsonValue.ValueType.OBJECT) {
                    if (matchPhone(js.asJsonObject(), target, selfId)) return true;
                }
            }
        } catch (Exception ignored) {}

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
                if (js.getValueType() == JsonValue.ValueType.ARRAY) {
                    if (matchAccount(js.asJsonArray(), target, selfId)) return true;
                } else if (js.getValueType() == JsonValue.ValueType.OBJECT) {
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

    /* ================= HTTP helpers (capture status + body) ================= */

    private static final class ApiResponse { final int status; final String body;
        ApiResponse(int s, String b){ this.status=s; this.body=b; } }

    private ApiResponse sendJsonWithStatus(String method, String path, String body) throws IOException {
        URL url = new URL(apiBase + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        if ("POST".equals(method) || "PUT".equals(method)) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String respBody = "";
        if (is != null) respBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        conn.disconnect();
        return new ApiResponse(status, respBody);
    }

    private static String parseErrorCode(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            var rdr = Json.createReader(new java.io.StringReader(json));
            JsonStructure js = rdr.read();
            if (js.getValueType() == JsonValue.ValueType.OBJECT) {
                JsonObject o = js.asJsonObject();
                if (o.containsKey("error") && !o.isNull("error")) return o.getString("error", null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /* ================= misc ================= */

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
        URL url = new URL(apiBase + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();
        conn.getResponseCode();
        conn.disconnect();
    }

    private static String safe(String v) { return v == null ? "" : v.trim(); }
}
