package com.documentapp.service;

import com.documentapp.tx.TransactionCallback;
import com.documentapp.tx.TransactionManager;

import java.io.File;
import java.sql.Connection;

public class FileUploadServiceProxy {

    private final FileUploadService target;
    private final TransactionManager transactionManager;

    public FileUploadServiceProxy() {
        this.target = new FileUploadServiceImpl();
        this.transactionManager = new TransactionManager();
    }

    public void mergeAndSave(final File folder,
                             final int totalChunks,
                             final long userId,
                             final String originalFileName,
                             final String notes,
                             final String fileIdParam) throws Exception {

        TransactionCallback<Void> callback = new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(Connection con) throws Exception {
                target.mergeAndSave(
                        folder,
                        totalChunks,
                        userId,
                        originalFileName,
                        notes,
                        fileIdParam,
                        con
                );
                return null;
            }
        };

        transactionManager.execute(callback);
    }
}