package com.pahanaedu.web.servlets;

import com.pahanaedu.web.api.ApiClient;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private String apiBase;

    @Override
    public void init() {
        this.apiBase = getServletContext().getInitParameter("apiBase");
        if (this.apiBase == null) {
            this.apiBase = "http://localhost:8080/pahana-edu-service/api";
        }
    }

    /** Show the new neumorphism login page (or redirect if already logged in). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s != null) {
            String role = (String) s.getAttribute("role");
            if ("ADMIN".equals(role)) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
                return;
            } else if ("CASHIER".equals(role)) {
                resp.sendRedirect(req.getContextPath() + "/cashier/dashboard.jsp");
                return;
            }
        }
        // ⬇️ forward to the new login page
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    /** Handle credentials, set session, and route by role. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null) username = "";
        if (password == null) password = "";

        try {
            ApiClient api = new ApiClient(apiBase);
            JsonObject payload = Json.createObjectBuilder()
                    .add("username", username)
                    .add("password", password)
                    .build();

            JsonStructure result = api.sendJson("POST", "/auth/login", payload.toString());

            if (result == null || result.getValueType() != JsonValue.ValueType.OBJECT) {
                req.setAttribute("error", "Unexpected response from the authentication service.");
                req.setAttribute("usernamePrefill", username);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            JsonObject user = result.asJsonObject();

            if (user.containsKey("error")) {
                req.setAttribute("error", user.getString("error", "Invalid username or password."));
                req.setAttribute("usernamePrefill", username);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            // ✅ Success: rotate session ID to prevent fixation and save user data
            HttpSession session = req.getSession(true);
            req.changeSessionId();                // available on Jakarta Servlet
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            session.setAttribute("userId",   user.getInt("id"));
            session.setAttribute("username", user.getString("username", username));
            session.setAttribute("role",     user.getString("role", ""));

            String role = (String) session.getAttribute("role");
            if ("ADMIN".equals(role)) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
            } else if ("CASHIER".equals(role)) {
                resp.sendRedirect(req.getContextPath() + "/cashier/dashboard.jsp");
            } else {
                // Unknown role – treat as error
                session.invalidate();
                req.setAttribute("error", "Your account does not have a valid role.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }

        } catch (Exception ex) {
            // Network/JSON/errors from service
            req.setAttribute("error", "Login service is unavailable. Please try again.");
            req.setAttribute("usernamePrefill", username);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
