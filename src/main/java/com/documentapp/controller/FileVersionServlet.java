package com.documentapp.controller;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.Files;
import com.documentapp.model.FileVersion;
import com.documentapp.util.DBConnection;
import com.documentapp.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public class FileVersionServlet extends HttpServlet {

    private static final int PAGE_SIZE = 15;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Connection con = null;

        try {

           
            Long userObj = AuthUtil.getUserIdFromCookie(request);

            if (userObj == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            long userId = userObj;

            con = DBConnection.getConnection();

            String fileIdParam = request.getParameter("fileId");

            if (fileIdParam == null) {
                response.sendRedirect("files");
                return;
            }

            long fileId;

            try {
                fileId = Long.parseLong(fileIdParam);
            } catch (Exception e) {
                response.sendRedirect("files");
                return;
            }

            FileDAO fileDAO = new FileDAO();
            Files file = fileDAO.getFileById(con, fileId);

            if (file == null || file.getUploadedBy() != userId) {
                response.sendRedirect("files");
                return;
            }

            String cursorParam = request.getParameter("cursor");
            Timestamp cursorTime = null;

            try {
                if (cursorParam != null && !cursorParam.isEmpty()) {
                    cursorTime = Timestamp.valueOf(cursorParam);
                }
            } catch (Exception ignored) {}

     
            String sortParam = request.getParameter("sort");
            boolean asc = "asc".equalsIgnoreCase(sortParam);


            FileVersionDAO dao = new FileVersionDAO();

            List<FileVersion> versionList =
                    dao.getVersionsByCursor(con, fileId, cursorTime, PAGE_SIZE, asc);

            if (cursorParam != null) {

                response.setContentType("text/html");
                PrintWriter out = response.getWriter();

                for (FileVersion v : versionList) {

                    String notes =
                            v.getNotes() == null ? "—" : escapeHtml(v.getNotes());

                    out.println("<tr>");
                    out.println("<td>v" + v.getVersionNumber() + "</td>");
                    out.println("<td>" + v.getUploadedTime() + "</td>");
                    out.println("<td>" + notes + "</td>");

                    out.println("<td>");
                    out.println("<a href='view?versionId=" + v.getId() + "'>View File</a> ");
                    out.println("<a href='download?versionId=" + v.getId() + "'>Download</a> ");
                    out.println("<a href='versionDetails?versionId=" + v.getId() + "'>View Details</a>");
                    out.println("</td>");

                    out.println("</tr>");
                }

                if (!versionList.isEmpty()) {

                    FileVersion last = versionList.get(versionList.size() - 1);

                    response.setHeader(
                            "nextCursor",
                            last.getUploadedTime().toString()
                    );
                }

                response.setHeader(
                        "hasMore",
                        versionList.size() < PAGE_SIZE ? "false" : "true"
                );

                return;
            }

            request.setAttribute("file", file);
            request.setAttribute("versions", versionList);
            request.setAttribute("fileId", fileId);

            request.getRequestDispatcher("versions.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();
            throw new ServletException(e);

        } finally {

            DBConnection.close(con);
        }
    }

    private String escapeHtml(String s) {

        if (s == null) return "";

        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;");
    }
}