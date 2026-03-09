package com.documentapp.controller;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.Files;
import com.documentapp.model.FileVersion;
import com.documentapp.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.Connection;

public class FileDownloadServlet extends HttpServlet {

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

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            FileVersionDAO versionDAO = new FileVersionDAO();
            FileVersion version;

            if (versionId > 0) {
                version = versionDAO.getVersionById(con, versionId);
            }
            else {
                version = versionDAO.getLatestVersionByFileId(con, fileId);
            }

            if (version == null) {
                response.sendRedirect("files");
                return;
            }

            FileDAO fileDAO = new FileDAO();
            Files file = fileDAO.getFileById(con, version.getFileId());

            if (file == null || file.getUploadedBy() != userId) {
                response.sendRedirect("files");
                return;
            }

            File physicalFile = new File(version.getFilePath());

            if (!physicalFile.exists()) {
                response.getWriter().println("File not found");
                return;
            }

            response.setContentType("application/octet-stream");
            response.setContentLengthLong(physicalFile.length());

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + physicalFile.getName() + "\""
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

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}