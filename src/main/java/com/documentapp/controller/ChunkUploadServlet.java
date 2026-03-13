package com.documentapp.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

import com.documentapp.util.AuthUtil;
import com.documentapp.service.FileUploadServiceProxy;

import java.io.*;

@MultipartConfig
public class ChunkUploadServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = AuthUtil.getUserIdFromCookie(request);

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("NOT_LOGGED_IN");
            return;
        }

        try {
            String uploadId = request.getParameter("uploadId");
            String fileName = request.getParameter("fileName");

            int chunkIndex = Integer.parseInt(request.getParameter("chunkIndex"));
            int totalChunks = Integer.parseInt(request.getParameter("totalChunks"));

            Part chunk = request.getPart("chunk");

            if (uploadId == null || chunk == null || fileName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("INVALID_REQUEST");
                return;
            }

            String baseTemp = System.getProperty("user.home")
                    + "/Downloads/DocumentApp/temp";

            File uploadFolder = new File(baseTemp, uploadId);

            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            File chunkFile = new File(uploadFolder, "chunk_" + chunkIndex);

            try (InputStream in = chunk.getInputStream();
                 FileOutputStream out = new FileOutputStream(chunkFile)) {

                byte[] buffer = new byte[8192];
                int len;

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            int chunkCount = uploadFolder.listFiles().length;

            if (chunkCount == totalChunks) {
                synchronized (uploadId.intern()) {
                    File[] chunks = uploadFolder.listFiles();

                    if (chunks != null && chunks.length == totalChunks) {
                        String fileIdParam = request.getParameter("fileId");
                        String notes = request.getParameter("notes");

                        FileUploadServiceProxy proxy = new FileUploadServiceProxy();

                        proxy.mergeAndSave(
                                uploadFolder,
                                totalChunks,
                                userId,
                                fileName,
                                notes,
                                fileIdParam
                        );

                        System.out.println("UPLOAD MERGED: " + fileName);

                        response.getWriter().write("MERGED");
                        return;
                    }
                }
            }

            response.getWriter().write("CHUNK_OK");

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("UPLOAD_ERROR");
        }
    }
}