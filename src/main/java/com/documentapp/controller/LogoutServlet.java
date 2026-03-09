package com.documentapp.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cookie cookie = new Cookie("userId", "");
        cookie.setMaxAge(0); 
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("home.jsp");
    }
}