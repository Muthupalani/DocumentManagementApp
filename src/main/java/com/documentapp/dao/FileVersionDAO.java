package com.documentapp.dao;

import com.documentapp.model.FileVersion;
import com.documentapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileVersionDAO {

	public int getNextVersionNumber(Connection con, long fileId) {

	    String sql =
	            "SELECT MAX(version_number) FROM file_versions WHERE file_id = ?";

	    int nextVersion = 1;

	    try {

	        ResultSet rs = DBConnection.executeQuery(con, sql, fileId);

	        if (rs.next() && rs.getInt(1) > 0) {
	            nextVersion = rs.getInt(1) + 1;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return nextVersion;
	}
    public boolean addFileVersion(Connection con, FileVersion version) {

        String sql =
                "INSERT INTO file_versions (file_id, version_number, uploaded_time, file_size, file_type, file_path, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {

            int rows = DBConnection.executeUpdate(
                    con,
                    sql,
                    version.getFileId(),
                    version.getVersionNumber(),
                    version.getUploadedTime(),
                    version.getFileSize(),
                    version.getFileType(),
                    version.getFilePath(),
                    version.getNotes()
            );

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public FileVersion getVersionById(Connection con, long versionId) {

        String sql = "SELECT * FROM file_versions WHERE id = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(con, sql, versionId);

            if (!rs.next()) return null;

            FileVersion version = new FileVersion();

            version.setId(rs.getLong("id"));
            version.setFileId(rs.getLong("file_id"));
            version.setVersionNumber(rs.getInt("version_number"));
            version.setUploadedTime(rs.getTimestamp("uploaded_time"));
            version.setFileSize(rs.getLong("file_size"));
            version.setFileType(rs.getString("file_type"));
            version.setFilePath(rs.getString("file_path"));
            version.setNotes(rs.getString("notes"));

            return version;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FileVersion getLatestVersionByFileId(Connection con, long fileId) {

        String sql =
                "SELECT * FROM file_versions WHERE file_id = ? ORDER BY version_number DESC LIMIT 1";

        try {

            ResultSet rs = DBConnection.executeQuery(con, sql, fileId);

            if (!rs.next()) return null;

            FileVersion version = new FileVersion();

            version.setId(rs.getLong("id"));
            version.setFileId(rs.getLong("file_id"));
            version.setVersionNumber(rs.getInt("version_number"));
            version.setUploadedTime(rs.getTimestamp("uploaded_time"));
            version.setFileSize(rs.getLong("file_size"));
            version.setFileType(rs.getString("file_type"));
            version.setFilePath(rs.getString("file_path"));
            version.setNotes(rs.getString("notes"));

            return version;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<FileVersion> getVersionsByCursor(Connection con,
                                                 long fileId,
                                                 Timestamp cursor,
                                                 int limit,
                                                 boolean asc) {

        List<FileVersion> list = new ArrayList<>();

        String sql;

        if (cursor == null) {

            sql =
                    "SELECT * FROM file_versions WHERE file_id = ? " +
                    "ORDER BY uploaded_time " + (asc ? "ASC" : "DESC") +
                    " LIMIT ?";

        } else {

            sql =
                    "SELECT * FROM file_versions WHERE file_id = ? AND uploaded_time " +
                    (asc ? "> ?" : "< ?") +
                    " ORDER BY uploaded_time " + (asc ? "ASC" : "DESC") +
                    " LIMIT ?";
        }

        try {

            PreparedStatement ps = con.prepareStatement(sql);

            if (cursor == null) {

                ps.setLong(1, fileId);
                ps.setInt(2, limit);

            } else {

                ps.setLong(1, fileId);
                ps.setTimestamp(2, cursor);
                ps.setInt(3, limit);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                FileVersion version = new FileVersion();

                version.setId(rs.getLong("id"));
                version.setFileId(rs.getLong("file_id"));
                version.setVersionNumber(rs.getInt("version_number"));
                version.setUploadedTime(rs.getTimestamp("uploaded_time"));
                version.setFileSize(rs.getLong("file_size"));
                version.setFileType(rs.getString("file_type"));
                version.setFilePath(rs.getString("file_path"));
                version.setNotes(rs.getString("notes"));

                list.add(version);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}