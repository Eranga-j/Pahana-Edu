package com.pahanaedu.web.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="LogoutServlet", urlPatterns={"/logout"})
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getSession(false) != null) req.getSession(false).invalidate();
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
