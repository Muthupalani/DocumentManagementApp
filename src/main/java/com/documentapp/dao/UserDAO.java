package com.documentapp.dao;

import com.documentapp.model.User;
import com.documentapp.util.DBConnection;
import com.documentapp.util.EncryptionUtil;
import com.documentapp.util.HashUtil;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    public boolean registerUser(Connection con, User user) {

        String sql =
                "INSERT INTO users (name, email, email_hash, password) VALUES (?, ?, ?, ?)";

        try {

            int rows = DBConnection.executeUpdate(
                    con,
                    sql,
                    EncryptionUtil.encrypt(user.getName()),
                    EncryptionUtil.encrypt(user.getEmail()),
                    HashUtil.hashEmail(user.getEmail()),
                    BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())
            );

            return rows == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public User loginUser(Connection con, String email, String password) {

        String sql =
                "SELECT id, name, email, password FROM users WHERE email_hash = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(
                    con,
                    sql,
                    HashUtil.hashEmail(email)
            );

            if (!rs.next()) return null;

            String dbPassword = rs.getString("password");

            if (!BCrypt.checkpw(password, dbPassword)) {
                return null;
            }

            User user = new User();

            user.setId(rs.getLong("id"));
            user.setName(EncryptionUtil.decrypt(rs.getString("name")));
            user.setEmail(EncryptionUtil.decrypt(rs.getString("email")));
            user.setPassword(dbPassword);

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean isEmailExists(Connection con, String email) {

        String sql =
                "SELECT id FROM users WHERE email_hash = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(
                    con,
                    sql,
                    HashUtil.hashEmail(email)
            );

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public User getUserById(Connection con, long id) {

        String sql =
                "SELECT id, name, email, password FROM users WHERE id = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(con, sql, id);

            if (!rs.next()) return null;

            User user = new User();

            user.setId(rs.getLong("id"));
            user.setName(EncryptionUtil.decrypt(rs.getString("name")));
            user.setEmail(EncryptionUtil.decrypt(rs.getString("email")));
            user.setPassword(rs.getString("password"));

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public User getUserByEmail(Connection con, String email) {

        String sql =
                "SELECT id, name, email, password FROM users WHERE email_hash = ?";

        try {

            ResultSet rs = DBConnection.executeQuery(
                    con,
                    sql,
                    HashUtil.hashEmail(email)
            );

            if (!rs.next()) return null;

            User user = new User();

            user.setId(rs.getLong("id"));
            user.setName(EncryptionUtil.decrypt(rs.getString("name")));
            user.setEmail(EncryptionUtil.decrypt(rs.getString("email")));
            user.setPassword(rs.getString("password"));

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}