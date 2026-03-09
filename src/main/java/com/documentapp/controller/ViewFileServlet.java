package com.documentapp.controller;

import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.FileVersion;
import com.documentapp.util.DBConnection;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.Connection;

public class ViewFileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Long userObj = (Long) request.getAttribute("userId");

        if (userObj == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        long versionId = -1;
        long fileId = -1;

        try {

            if (request.getParameter("versionId") != null) {
                versionId = Long.parseLong(request.getParameter("versionId"));
            }
            else if (request.getParameter("fileId") != null) {
                fileId = Long.parseLong(request.getParameter("fileId"));
            }
            else {
                response.sendRedirect("files");
                return;
            }

        } catch (Exception e) {
            response.sendRedirect("files");
            return;
        }

        FileVersionDAO versionDAO = new FileVersionDAO();
        FileVersion version = null;

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            if (versionId > 0) {
                version = versionDAO.getVersionById(con, versionId);
            } else {
                version = versionDAO.getLatestVersionByFileId(con, fileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(con);
        }

        if (version == null) {
            response.sendRedirect("files");
            return;
        }

        File physicalFile = new File(version.getFilePath());

        if (!physicalFile.exists()) {
            response.getWriter().println("File not found");
            return;
        }

        String mimeType =
                getServletContext().getMimeType(physicalFile.getName());

        if (mimeType == null)
            mimeType = "application/octet-stream";

        response.setContentType(mimeType);
        response.setContentLengthLong(physicalFile.length());

        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + physicalFile.getName() + "\""
        );

        try (BufferedInputStream in =
                     new BufferedInputStream(new FileInputStream(physicalFile));
             BufferedOutputStream out =
                     new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }
    }
}