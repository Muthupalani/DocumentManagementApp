package com.documentapp.tx;

import com.documentapp.util.DBConnection;

import java.sql.Connection;

public class TransactionManager {

    public <T> T execute(TransactionCallback<T> callback) throws Exception {
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            T result = callback.doInTransaction(con);

            con.commit();
            return result;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            throw e;

        } finally {
            DBConnection.close(con);
        }
    }
}