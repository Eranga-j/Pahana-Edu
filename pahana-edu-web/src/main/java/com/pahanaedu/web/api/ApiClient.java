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

    /* ======================= Public API ======================= */

    /** GET that expects a JSON response */
    public JsonStructure getJson(String path) throws IOException {
        HttpURLConnection con = open("GET", path, null);
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code >= 200 && code < 300) {
            return parseJsonOrThrow("GET " + path, body);
        }
        throw new IOException("GET " + path + " failed: HTTP " + code + " - " + body);
    }

    /** POST/PUT/DELETE when you DON'T need JSON back (e.g., returns 204 No Content) */
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

    /** POST/PUT when you DO need JSON back (e.g., create bill returns the bill) */
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

    /**
     * DELETE with **no request body and no Content-Type**.
     * Many servers return 400 if a DELETE is sent with a JSON content-type but empty body.
     */
    public void delete(String path) throws IOException {
        HttpURLConnection con = open("DELETE", path, null); // no Content-Type, no doOutput
        int code = con.getResponseCode();
        String body = readBody((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

        if (code >= 200 && code < 300) {
            // success; nothing to return
            return;
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
