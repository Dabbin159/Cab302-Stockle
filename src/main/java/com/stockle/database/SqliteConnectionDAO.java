package com.stockle.database;

import java.sql.Connection;
import java.sql.SQLException;

public class SqliteConnectionDAO {

    private Connection connection;

    public SqliteConnectionDAO() {
        connection = SqliteConnection.getInstance();
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}
