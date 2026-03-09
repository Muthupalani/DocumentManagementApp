package com.documentapp.controller;

import com.documentapp.dao.UserDAO;
import com.documentapp.model.User;
import com.documentapp.util.DBConnection;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class LoginServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("login.jsp")
                .forward(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO dao = new UserDAO();
        User user = null;

        try (Connection con = DBConnection.getConnection()) {

            user = dao.loginUser(con, email, password);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user != null) {

            Cookie cookie = new Cookie("userId", String.valueOf(user.getId()));
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);

            response.sendRedirect("upload.jsp");

        } else {

            request.setAttribute("error", "Invalid Credentials");
            request.getRequestDispatcher("login.jsp")
                    .forward(request, response);
        }
    }
}