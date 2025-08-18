package com.pahanaedu.web.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RoleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String role = session != null ? (String) session.getAttribute("role") : null;
        String path = req.getRequestURI();
        if (path.contains("/admin/")) {
            if (!"ADMIN".equals(role)) { resp.sendError(403); return; }
        } else if (path.contains("/cashier/")) {
            if (!"CASHIER".equals(role) && !"ADMIN".equals(role)) { resp.sendError(403); return; }
        }
        chain.doFilter(request, response);
    }
}
