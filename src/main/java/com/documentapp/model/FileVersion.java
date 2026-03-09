package com.documentapp.model;

import java.sql.Timestamp;

public class FileVersion {

    private long id;
    private long fileId;
    private int versionNumber;
    private Timestamp uploadedTime;
    private long fileSize;
    private String fileType;
    private String filePath;
    private String notes;

    public FileVersion() { }
    
    public long getId() { return id;}
    public void setId(long id) { this.id = id; }
    
    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId;}
    
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    
    public Timestamp getUploadedTime() { return uploadedTime; }
    public void setUploadedTime(Timestamp uploadedTime) { this.uploadedTime = uploadedTime; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getFileType() { return fileType;}
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public String getFilePath() { return filePath;}
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
}