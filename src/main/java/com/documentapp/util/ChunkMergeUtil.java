package com.documentapp.util;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.Files;
import com.documentapp.model.FileVersion;

import java.io.*;
import java.sql.Connection;
import java.sql.Timestamp;

public class ChunkMergeUtil {

    public static void mergeAndSave(File folder, int totalChunks,
                                    long userId, String fileName,
                                    String notes, String fileIdParam){

        Connection con = null;
        try{
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String baseFolder = System.getProperty("user.home")
                    + File.separator + "Downloads"
                    + File.separator + "DocumentApp";
            String userFolderPath = baseFolder + File.separator + "user_" + userId;
            File userFolder = new File(userFolderPath);
            if(!userFolder.exists()) userFolder.mkdirs();

            File mergedFile = new File(userFolder, fileName);
            try(FileOutputStream fos = new FileOutputStream(mergedFile)){
                for(int i=0;i<totalChunks;i++){
                    File chunk = new File(folder,"chunk_"+i);
                    try(FileInputStream fis = new FileInputStream(chunk)){
                        byte[] buffer = new byte[8192];
                        int len;
                        while((len=fis.read(buffer))!=-1){
                            fos.write(buffer,0,len);
                        }
                    }
                }
            }

            // Insert into DB
            FileDAO fileDAO = new FileDAO();
            FileVersionDAO versionDAO = new FileVersionDAO();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            long fileId;
            boolean isNewFile = (fileIdParam==null || fileIdParam.isEmpty());

            if(isNewFile){
                Files file = new Files();
                file.setFileName(fileName);
                file.setFileStorage(mergedFile.length());
                file.setUploadedBy(userId);
                file.setUploadedTime(now);
                file.setModifiedTime(now);

                fileId = fileDAO.createFile(con,file);
            } else {
                fileId = Long.parseLong(fileIdParam);
                Files existing = fileDAO.getFileById(con,fileId);
                if(existing==null || existing.getUploadedBy()!=userId){
                    throw new RuntimeException("Invalid file or permission denied");
                }
            }

            int nextVersion = versionDAO.getNextVersionNumber(con,fileId);
            FileVersion version = new FileVersion();
            version.setFileId(fileId);
            version.setVersionNumber(nextVersion);
            version.setUploadedTime(now);
            version.setFileSize(mergedFile.length());
            version.setFileType("application/octet-stream");
            version.setFilePath(mergedFile.getAbsolutePath());
            version.setNotes(notes);

            versionDAO.addFileVersion(con,version);

            if(!isNewFile){
                fileDAO.updateFileAfterNewVersion(con,fileId,mergedFile.length(),now);
            }

            con.commit();

            // Delete temp chunks
            for(File f : folder.listFiles()) f.delete();
            folder.delete();

        }catch(Exception e){
            try{ if(con!=null) con.rollback(); }catch(Exception ex){ ex.printStackTrace(); }
            e.printStackTrace();
        }finally{
            DBConnection.close(con);
        }
    }
}