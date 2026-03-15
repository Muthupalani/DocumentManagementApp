package com.documentapp.service;

import java.io.File;
import java.sql.Connection;

public interface FileUploadService {

    void mergeAndSave(File folder,
                      int totalChunks,
                      long userId,
                      String originalFileName,
                      String notes,
                      String fileIdParam,
                      Connection con) throws Exception;
}