package com.documentapp.dao;

import com.documentapp.model.Files;
import com.documentapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public long createFile(Connection con, Files file) {

        String sql =
                "INSERT INTO files (file_name, file_storage, uploaded_by, uploaded_time, modified_time) VALUES (?, ?, ?, ?, ?)";
        try {
            return DBConnection.executeInsertAndReturnId(
                    con,
                    sql,
                    file.getFileName(),
                    file.getFileStorage(),
                    file.getUploadedBy(),
                    file.getUploadedTime(),
                    file.getModifiedTime()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public List<Files> getFilesByCursor(Connection con,long userId,Timestamp cursor,int limit, boolean asc) {

        List<Files> list = new ArrayList<>();
        String sql;

        if (cursor == null) {

        	sql = "SELECT f.*, MAX(fv.version_number) AS latest_version " +
        			"FROM files f JOIN file_versions fv ON f.file_id = fv.file_id " +
        			"WHERE f.uploaded_by=? " +
        			"GROUP BY f.file_id " +
        			"ORDER BY f.modified_time " + (asc ? "ASC" : "DESC") + " " +
        			"LIMIT ?";

        } else {

        	sql ="SELECT f.*, MAX(fv.version_number) AS latest_version " +
        			"FROM files f JOIN file_versions fv ON f.file_id = fv.file_id " +
        			"WHERE f.uploaded_by=? AND f.modified_time " + (asc ? "> ?" : "< ?") + " " +
        			"GROUP BY f.file_id, f.modified_time " +
        			"ORDER BY f.modified_time " + (asc ? "ASC" : "DESC") + " " +
        			"LIMIT ?";
        }

        try {

            PreparedStatement ps = con.prepareStatement(sql);

            if (cursor == null) {

                ps.setLong(1, userId);
                ps.setInt(2, limit);

            } else {

                ps.setLong(1, userId);
                ps.setTimestamp(2, cursor);
                ps.setInt(3, limit);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Files file = new Files();
            
                file.setFileId(rs.getLong("file_id"));
                file.setFileName(rs.getString("file_name"));
                file.setLatestVersion(rs.getInt("latest_version"));
                file.setFileStorage(rs.getLong("file_storage"));
                file.setUploadedBy(rs.getLong("uploaded_by"));
                file.setUploadedTime(rs.getTimestamp("uploaded_time"));
                file.setModifiedTime(rs.getTimestamp("modified_time"));
                System.out.println("Object version: " + file.getLatestVersion());
                

                list.add(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Files getFileById(Connection con, long fileId) {
        String sql =
                "SELECT * FROM files WHERE file_id = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(con, sql, fileId);

            if (!rs.next()) return null;

            Files file = new Files();

            file.setFileId(rs.getLong("file_id"));
            file.setFileName(rs.getString("file_name"));
            file.setFileStorage(rs.getLong("file_storage"));
            file.setUploadedBy(rs.getLong("uploaded_by"));
            file.setUploadedTime(rs.getTimestamp("uploaded_time"));
            file.setModifiedTime(rs.getTimestamp("modified_time"));

            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void updateFileAfterNewVersion(Connection con, long fileId, long newVersionSize, Timestamp modifiedTime) {

        String sql =
                "UPDATE files SET file_storage = file_storage + ?, modified_time = ? WHERE file_id = ?";

        try {

            DBConnection.executeUpdate(
                    con,
                    sql,
                    newVersionSize,
                    modifiedTime,
                    fileId
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}