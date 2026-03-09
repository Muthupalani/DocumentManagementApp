package com.documentapp.util;

import java.sql.*;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/document_app?useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "Ramasamy@zoho@1413";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException("MySQL Driver Load Failed", e);
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }



    private static PreparedStatement prepareStatement(Connection con,
                                                       String sql,
                                                       Object... params) throws SQLException {

        PreparedStatement ps = con.prepareStatement(sql);

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }

        return ps;
    }


    public static ResultSet executeQuery(Connection con,
                                         String sql,
                                         Object... params) throws SQLException {

        PreparedStatement ps = prepareStatement(con, sql, params);
        return ps.executeQuery();
    }


    public static int executeUpdate(Connection con,
                                    String sql,
                                    Object... params) throws SQLException {

        try (PreparedStatement ps = prepareStatement(con, sql, params)) {
            return ps.executeUpdate();
        }
    }

    // ==============================
    // INSERT + RETURN GENERATED ID
    // ==============================

    public static long executeInsertAndReturnId(Connection con,
                                                String sql,
                                                Object... params) throws SQLException {

        try (PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return -1;
    }

    // ==============================
    // SAFE CLOSE METHODS
    // ==============================

    public static void close(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (Exception ignored) {}
    }

    public static void close(Statement st) {
        try {
            if (st != null) st.close();
        } catch (Exception ignored) {}
    }

    public static void close(Connection con) {
        try {
            if (con != null) con.close();
        } catch (Exception ignored) {}
    }
}