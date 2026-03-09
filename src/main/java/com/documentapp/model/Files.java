package com.documentapp.model;

import java.sql.Timestamp;

public class Files {

    private long Id;
    private String Name;
    private long Storage;
    private long uploadedBy;
    private Timestamp uploadedTime;
    private Timestamp modifiedTime;
    private int latestVersion;

    public Files() { }
    
    public long getFileId() {return Id; }
    public void setFileId(long Id) {this.Id = Id; }

    public String getFileName() {return Name;}
    public void setFileName(String Name) { this.Name = Name;}
    
    
    public long getFileStorage() { return Storage;}
    public void setFileStorage(long Storage) { this.Storage = Storage;}
    
    public long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(long uploadedBy) { this.uploadedBy = uploadedBy;}
    
    public Timestamp getUploadedTime() { return uploadedTime; }
    public void setUploadedTime(Timestamp uploadedTime) { this.uploadedTime = uploadedTime; }
    
    public Timestamp getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(Timestamp modifiedTime) { this.modifiedTime = modifiedTime;}
    
    public int getLatestVersion() { return latestVersion; }
    public void setLatestVersion(int latestVersion) { this.latestVersion = latestVersion;}
    
}