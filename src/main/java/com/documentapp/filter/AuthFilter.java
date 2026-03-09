package com.documentapp.filter;

import com.documentapp.util.AuthUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.equals("/") ||
            path.startsWith("/login") ||
            path.startsWith("/register") ||
            path.startsWith("/signup.jsp") ||
            path.startsWith("/home.jsp")) {

            chain.doFilter(request, response);
            return;
        }
        
        
     
        Long userId = AuthUtil.getUserIdFromCookie(req);

        if (userId == null) {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        req.setAttribute("userId", userId);
        chain.doFilter(request, response);  
    }
}