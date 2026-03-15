package com.documentapp.tx;

import java.sql.Connection;

public interface TransactionCallback<T> {
    T doInTransaction(Connection con) throws Exception;
}