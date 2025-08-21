package com.pahanaedu.web.api;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    /* ==========================================================
     *  New: response wrapper (status + body [+ parsed JSON])
     * ========================================================== */
    public static final class ApiResponse {
        public final int status;
        public final String body;
        /** Parsed JSON if the body is valid JSON, otherwise null. */
        public final JsonStructure json;

        public ApiResponse(int status, String body, JsonStructure json) {
            this.status = status;
            this.body = body;
            this.json = json;
        }

        public boolean is2xx() { return status >= 200 && status < 300; }
    }

    /* ==========================================================
     *  Non-throwing helpers (prefer these in servlets)
     * ========================================================== */

    /** GET — returns status + body; parses JSON if present. */
    public ApiResponse get(String path) throws IOException {
        HttpURLConnection con = open("GET", path, null);
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());
        return new ApiResponse(code, body, tryParseJson(body));
    }

    /** POST/PUT/PATCH with JSON request; returns status + body; parses JSON if present. */
    public ApiResponse send(String method, String path, String jsonBody) throws IOException {
        HttpURLConnection con = open(method, path, "application/json");
        con.setDoOutput(true);
        if (jsonBody != null) {
            try (OutputStream os = con.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());
        return new ApiResponse(code, body, tryParseJson(body));
    }

    /** DELETE with no request body/Content-Type; returns status + body; parses JSON if present. */
    public ApiResponse deleteForStatus(String path) throws IOException {
        HttpURLConnection con = open("DELETE", path, null);
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());
        return new ApiResponse(code, body, tryParseJson(body));
    }

    /* ==========================================================
     *  Backward compatible strict methods (throw on non-2xx)
     * ========================================================== */

    /** GET that expects a JSON response (throws on non-2xx or non-JSON). */
    public JsonStructure getJson(String path) throws IOException {
        HttpURLConnection con = open("GET", path, null);
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code >= 200 && code < 300) {
            return parseJsonOrThrow("GET " + path, body);
        }
        throw new IOException("GET " + path + " failed: HTTP " + code + " - " + body);
    }

    /** POST/PUT/DELETE when you DON'T need JSON back (throws on non-2xx). */
    public void sendJsonNoBody(String method, String path, String json) throws IOException {
        HttpURLConnection con = open(method, path, "application/json");
        con.setDoOutput(true);
        if (json != null) {
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = con.getResponseCode();
        // Drain any body (if present) to free connection, but do not parse
        readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code < 200 || code >= 300) {
            throw new IOException(method + " " + path + " failed: HTTP " + code);
        }
    }

    /** POST/PUT that expects JSON back (throws on non-2xx or non-JSON). */
    public JsonStructure sendJsonForJson(String method, String path, String json) throws IOException {
        HttpURLConnection con = open(method, path, "application/json");
        con.setDoOutput(true);
        if (json != null) {
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code >= 200 && code < 300) {
            return parseJsonOrThrow(method + " " + path, body);
        }
        throw new IOException(method + " " + path + " failed: HTTP " + code + " - " + body);
    }

    /** Backward compatibility: old callers still work (expects JSON back). */
    public JsonStructure sendJson(String method, String path, String json) throws IOException {
        return sendJsonForJson(method, path, json);
    }

    /** Legacy strict DELETE (throws on non-2xx). */
    public void delete(String path) throws IOException {
        HttpURLConnection con = open("DELETE", path, null); // no Content-Type, no doOutput
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code >= 200 && code < 300) {
            return; // success
        }
        throw new IOException("DELETE " + path + " failed: HTTP " + code + (body == null || body.isBlank() ? "" : " - " + body));
    }

    /* ======================= Helpers ======================= */

    private HttpURLConnection open(String method, String path, String contentType) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Accept", "application/json");
        if (contentType != null) {
            con.setRequestProperty("Content-Type", contentType);
        }
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        return con;
    }

    private static String readBody(InputStream in) throws IOException {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static JsonStructure tryParseJson(String body) {
        if (body == null || body.isBlank()) return null;
        try (StringReader sr = new StringReader(body);
             JsonReader jr = Json.createReader(sr)) {
            return jr.read();
        } catch (Exception ignore) {
            return null; // not JSON
        }
    }

    private static JsonStructure parseJsonOrThrow(String ctx, String body) throws IOException {
        try (StringReader sr = new StringReader(body);
             JsonReader jr = Json.createReader(sr)) {
            return jr.read();
        } catch (Exception ex) {
            String preview = body == null ? "" : body.substring(0, Math.min(body.length(), 300));
            throw new IOException(ctx + " expected JSON but got: " + preview, ex);
        }
    }
}
