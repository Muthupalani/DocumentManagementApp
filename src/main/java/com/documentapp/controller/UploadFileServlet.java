package com.documentapp.controller;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.Files;
import com.documentapp.model.FileVersion;
import com.documentapp.util.DBConnection;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;

public class UploadFileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Long userIdObj = (Long) request.getAttribute("userId");

        if (userIdObj == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        long userId = userIdObj;

        Part filePart = request.getPart("file");
        String notes = request.getParameter("notes");
        String fileIdParam = request.getParameter("fileId");

        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("files");
            return;
        }

        String originalFileName = filePart.getSubmittedFileName();
        String fileType = filePart.getContentType();
        long uploadedFileSize = filePart.getSize();

        FileDAO fileDAO = new FileDAO();
        FileVersionDAO versionDAO = new FileVersionDAO();

        Connection con = null;

        try {

            con = DBConnection.getConnection();
            con.setAutoCommit(false); 
            long fileId;
            String fileFolderName;

            Timestamp now = new Timestamp(System.currentTimeMillis());
            boolean isNewFile = (fileIdParam == null || fileIdParam.isEmpty());

            if (isNewFile) {

                fileFolderName = removeExtension(originalFileName);

                Files file = new Files();
                file.setFileName(fileFolderName);
                file.setFileStorage(uploadedFileSize);
                file.setUploadedBy(userId);
                file.setUploadedTime(now);
                file.setModifiedTime(now);

                fileId = fileDAO.createFile(con, file);

                if (fileId <= 0) {
                    throw new Exception("File insert failed");
                }

            } else {

                fileId = Long.parseLong(fileIdParam);

                Files existingFile = fileDAO.getFileById(con, fileId);

                if (existingFile == null || existingFile.getUploadedBy() != userId) {
                    response.sendRedirect("files");
                    return;
                }

                fileFolderName = existingFile.getFileName();
            }

            int nextVersionNumber = versionDAO.getNextVersionNumber(con, fileId);

            String baseFolder =
                    System.getProperty("user.home")
                            + File.separator + "Downloads"
                            + File.separator + "DocumentApp";

            String userFolder = baseFolder + File.separator + "user_" + userId;
            String fileFolder = userFolder + File.separator + fileFolderName;

            File dir = new File(fileFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String versionFileName = "v" + nextVersionNumber + "_" + originalFileName;

            String fullPath = fileFolder + File.separator + versionFileName;

            filePart.write(fullPath);

            FileVersion version = new FileVersion();
            version.setFileId(fileId);
            version.setVersionNumber(nextVersionNumber);
            version.setUploadedTime(now);
            version.setFileSize(uploadedFileSize);
            version.setFileType(fileType);
            version.setFilePath(fullPath);
            version.setNotes(notes);

            boolean inserted = versionDAO.addFileVersion(con, version);

            if (!inserted) {
                throw new Exception("Version insert failed");
            }

            if (!isNewFile) {
                fileDAO.updateFileAfterNewVersion(con, fileId, uploadedFileSize, now);
            }

            con.commit(); 

        } catch (Exception e) {

            e.printStackTrace();

            try {
                if (con != null) {
                    con.rollback(); 
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } finally {

            DBConnection.close(con);

        }

        response.sendRedirect("files");
    }

    private String removeExtension(String fileName) {

        if (fileName == null) return "file";

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex > 0)
            return fileName.substring(0, dotIndex);

        return fileName;
    }
}