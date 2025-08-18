package com.pahanaedu.web.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String path = req.getRequestURI();
        boolean loggedIn = (session != null && session.getAttribute("username") != null);
        boolean loginRequest = path.endsWith("/") || path.endsWith("/index.jsp") || path.endsWith("/login");
        boolean staticRes = path.contains("/common/") || path.endsWith(".css") || path.endsWith(".js");
        if (loggedIn || loginRequest || staticRes) chain.doFilter(request, response);
        else resp.sendRedirect(req.getContextPath() + "/");
    }
}
