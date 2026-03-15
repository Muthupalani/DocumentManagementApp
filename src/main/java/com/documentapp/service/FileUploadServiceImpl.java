package com.documentapp.service;

import com.documentapp.dao.FileDAO;
import com.documentapp.dao.FileVersionDAO;
import com.documentapp.model.FileVersion;
import com.documentapp.model.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Timestamp;

public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public void mergeAndSave(File folder,
                             int totalChunks,
                             long userId,
                             String originalFileName,
                             String notes,
                             String fileIdParam,
                             Connection con) throws Exception {

        String baseFolder = System.getProperty("user.home") + "/Downloads/DocumentApp";
        String userFolderPath = baseFolder + "/user_" + userId;

        File userFolder = new File(userFolderPath);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        boolean isNewFile = (fileIdParam == null || fileIdParam.isEmpty());

        long fileId;

        FileDAO fileDAO = new FileDAO();
        FileVersionDAO versionDAO = new FileVersionDAO();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        String fileNameWithoutExt = removeExtension(originalFileName);

        if (isNewFile) {
            Files file = new Files();
            file.setFileName(fileNameWithoutExt);
            file.setFileStorage(0);
            file.setUploadedBy(userId);
            file.setUploadedTime(now);
            file.setModifiedTime(now);

            fileId = fileDAO.createFile(con, file);

            if (fileId <= 0) {
                throw new Exception("Failed to create file metadata");
            }

            notes = null;

        } else {
            fileId = Long.parseLong(fileIdParam);

            Files existingFile = fileDAO.getFileById(con, fileId);

            if (existingFile == null || existingFile.getUploadedBy() != userId) {
                throw new Exception("Invalid file or permission denied");
            }

            fileNameWithoutExt = existingFile.getFileName();
        }

        File fileFolder = new File(userFolderPath + "/file_" + fileId);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        int nextVersionNumber = versionDAO.getNextVersionNumber(con, fileId);

        File versionFolder = new File(fileFolder, "v" + nextVersionNumber);
        if (!versionFolder.exists()) {
            versionFolder.mkdirs();
        }

        File mergedFile = new File(versionFolder, originalFileName);

        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (int i = 0; i < totalChunks; i++) {
                File chunk = new File(folder, "chunk_" + i);

                try (FileInputStream fis = new FileInputStream(chunk)) {
                    byte[] buffer = new byte[8192];
                    int len;

                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }

        String fileType = java.nio.file.Files.probeContentType(mergedFile.toPath());
        if (fileType == null) {
            fileType = "application/octet-stream";
        }

        FileVersion version = new FileVersion();
        version.setFileId(fileId);
        version.setVersionNumber(nextVersionNumber);
        version.setUploadedTime(now);
        version.setFileSize(mergedFile.length());
        version.setFileType(fileType);
        version.setFilePath(mergedFile.getAbsolutePath());
        version.setNotes(notes);

        boolean versionInserted = versionDAO.addFileVersion(con, version);

        if (!versionInserted) {
            throw new Exception("Failed to insert file version");
        }

        fileDAO.updateFileAfterNewVersion(con, fileId, mergedFile.length(), now);

        File[] tempFiles = folder.listFiles();
        if (tempFiles != null) {
            for (File f : tempFiles) {
                f.delete();
            }
        }
        folder.delete();

        System.out.println("FileUploadServiceImpl: upload completed");
    }

    private String removeExtension(String fileName) {
        if (fileName == null) return "file";

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }

        return fileName;
    }
}