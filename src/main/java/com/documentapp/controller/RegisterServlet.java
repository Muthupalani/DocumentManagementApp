package com.documentapp.controller;

import com.documentapp.dao.UserDAO;
import com.documentapp.model.User;
import com.documentapp.util.DBConnection;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO dao = new UserDAO();

        try (Connection con = DBConnection.getConnection()) {

            if (dao.isEmailExists(con, email)) {

                request.setAttribute("error", "Email already exists");
                request.getRequestDispatcher("signup.jsp")
                        .forward(request, response);
                return;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);

            boolean status = dao.registerUser(con, user);

            if (status) {

                User registeredUser = dao.getUserByEmail(con, email);

                if (registeredUser != null) {

                    Cookie cookie = new Cookie(
                            "userId",
                            String.valueOf(registeredUser.getId())
                    );

                    cookie.setMaxAge(60 * 60);
                    response.addCookie(cookie);

                    response.sendRedirect("upload.jsp");

                } else {

                    request.setAttribute("error", "Cannot fetch registered user");
                    request.getRequestDispatcher("signup.jsp")
                            .forward(request, response);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}