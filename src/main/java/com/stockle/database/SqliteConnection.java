package com.stockle.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to manage the connection to the SQLite database. Implements the Singleton pattern to ensure only one connection is used throughout the application.
 */
public class SqliteConnection {

    private static Connection instance = null;

    /**
     * Constructor to create connection using JDBC SQLite driver
     */
    private SqliteConnection() {
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
            databaseSetup();
        }
        return instance;
    }

    private static final String USER_TABLE = "CREATE TABLE IF NOT EXISTS users ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "username TEXT NOT NULL UNIQUE,"
            + "password TEXT NOT NULL,"
            + "email TEXT NOT NULL,"
            + "fullName TEXT NOT NULL,"
            + "dateOfBirth TEXT NOT NULL,"
            + "balance INTEGER NOT NULL,"
            + "totalProfit INTEGER NOT NULL"
            + ")";

    private static final String TRADE_TABLE = "CREATE TABLE IF NOT EXISTS trades ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userID INTEGER NOT NULL,"
            + "companyID TEXT NOT NULL,"
            + "tradeData TEXT NOT NULL,"
            + "createdAt TEXT NOT NULL,"
            + "sold INTEGER NOT NULL,"
            + "profit INTEGER,"
            + "quantity INTEGER NOT NULL,"
            + "FOREIGN KEY (userID) REFERENCES users(id)"
            + ")";

    private static final String HOLDING_TABLE = "CREATE TABLE IF NOT EXISTS holdings ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userID INTEGER NOT NULL,"
            + "companyID TEXT NOT NULL,"
            + "quantity INTEGER NOT NULL,"
            + "averagePrice INTEGER NOT NULL,"
            + "FOREIGN KEY (userID) REFERENCES users(id)"
            + ")";

    private static final String FAVOURITES_TABLE = "CREATE TABLE IF NOT EXISTS favourites ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userID INTEGER NOT NULL,"
            + "favouritesList TEXT NOT NULL,"
            + "FOREIGN KEY (userID) REFERENCES users(id)"
            + ")";

    /**
     * Method to set up the database tables if they do not already exist.
     */
    private static void databaseSetup() {
        try {
            Statement statement = instance.createStatement();
            statement.execute(USER_TABLE);
            statement.execute(TRADE_TABLE);
            statement.execute(HOLDING_TABLE);
            statement.execute(FAVOURITES_TABLE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}