package com.documentapp.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import com.documentapp.util.ChunkMergeUtil;

import java.io.*;

@MultipartConfig
public class ChunkUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Long userIdObj = (Long) request.getSession().getAttribute("userId");
        if(userIdObj == null){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("User not logged in");
            return;
        }
        long userId = userIdObj;

        try {
            String uploadId = request.getParameter("uploadId");
            int chunkIndex = Integer.parseInt(request.getParameter("chunkIndex"));
            int totalChunks = Integer.parseInt(request.getParameter("totalChunks"));
            Part chunk = request.getPart("chunk");

            String baseTemp = System.getProperty("user.home")
                    + File.separator + "Downloads"
                    + File.separator + "DocumentApp"
                    + File.separator + "temp";

            File uploadFolder = new File(baseTemp + File.separator + uploadId);
            if(!uploadFolder.exists()) uploadFolder.mkdirs();

            File chunkFile = new File(uploadFolder, "chunk_" + chunkIndex);
            try(InputStream in = chunk.getInputStream();
                FileOutputStream out = new FileOutputStream(chunkFile)){
                byte[] buffer = new byte[8192];
                int len;
                while((len=in.read(buffer))!=-1){
                    out.write(buffer,0,len);
                }
            }

            File[] uploadedChunks = uploadFolder.listFiles();
            if(uploadedChunks != null && uploadedChunks.length == totalChunks){
                String fileName = request.getParameter("fileName");
                String notes = request.getParameter("notes");
                String fileIdParam = request.getParameter("fileId");

                ChunkMergeUtil.mergeAndSave(uploadFolder,totalChunks,
                        userId, fileName, notes, fileIdParam);
            }

            response.getWriter().write("OK");

        } catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: "+e.getMessage());
        }
    }
}