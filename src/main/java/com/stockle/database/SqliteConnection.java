package com.stockle.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteConnection {

    private static Connection instance = null;

    /**
     * Constructor to create connection using JDBC SQLite driver
     */
    public SqliteConnection() {
        String url = "jdbc:sqlite:stockle.db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage() + " - " + ex.toString());
        }
    }

    /**
     * Method to get instance of a SQL Connection
     * @return new SQLiteConnection
     */
    public static Connection getInstance() {
        if (instance ==null) {
            new SqliteConnection(); // Create new connection if it doesn't exist
            databaseSetup(); // Ensure database is set up when connection is first created
        }
        return instance;
    }

    private static final String USER_TABLE = "CREATE TABLE IF NOT EXISTS users ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "username TEXT NOT NULL,"
            + "password TEXT NOT NULL,"
            + "email TEXT NOT NULL,"
            + "firstName TEXT NOT NULL,"
            + "lastName TEXT NOT NULL,"
            + "dateOfBirth TEXT NOT NULL"
            + ")";


    private static void databaseSetup() {
        try {
            Statement statement = instance.createStatement();
            statement.execute(USER_TABLE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}