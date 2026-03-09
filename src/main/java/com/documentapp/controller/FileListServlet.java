package com.documentapp.controller;

import com.documentapp.dao.FileDAO;
import com.documentapp.model.Files;
import com.documentapp.util.DBConnection;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public class FileListServlet extends HttpServlet {

    private static final int PAGE_SIZE = 15;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Long userObj = (Long) request.getAttribute("userId");

        if (userObj == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        long userId = userObj;

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            String cursorParam = request.getParameter("cursor");
            Timestamp cursor = null;

            if (cursorParam != null && !cursorParam.isEmpty()) {
                cursor = Timestamp.valueOf(cursorParam);
            }

            String sortParam = request.getParameter("sort");
            boolean asc = "asc".equalsIgnoreCase(sortParam);

            FileDAO fileDAO = new FileDAO();

            List<Files> files =
                    fileDAO.getFilesByCursor(con, userId, cursor, PAGE_SIZE, asc);

            if (cursorParam != null) {

                response.setContentType("text/html");
                PrintWriter out = response.getWriter();

                for (Files file : files) {

                    out.println("<tr>");
                    out.println("<td>" + file.getFileName() + "</td>");
                    out.println("<td>" + file.getLatestVersion() + "</td>");
                    out.println("<td>" + file.getModifiedTime() + "</td>");

                    out.println("<td>");
                    out.println("<a href='view?fileId=" + file.getFileId() + "'>View File</a> ");
                    out.println("<a href='download?fileId=" + file.getFileId() + "'>Download</a> ");
                    out.println("<a href='versions?fileId=" + file.getFileId() + "'>View Versions</a> ");
                    out.println("<a href='upload.jsp?fileId=" + file.getFileId() + "'>Upload New Version</a>");
                    out.println("</td>");

                    out.println("</tr>");
                }

                if (!files.isEmpty()) {

                    Timestamp last =
                            files.get(files.size() - 1).getModifiedTime();

                    response.setHeader("nextCursor", last.toString());
                }

                response.setHeader(
                        "hasMore",
                        files.size() < PAGE_SIZE ? "false" : "true"
                );

                return;
            }

            request.setAttribute("files", files);
            request.getRequestDispatcher("files.jsp").forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();
            response.sendRedirect("error.jsp");

        } finally {

            DBConnection.close(con);
        }
    }
}