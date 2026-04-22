package com.stockle.test;

import com.stockle.database.SqliteUserDAO;

public class DB_Test {

    public static void main(String[] args) {
        testDatabaseConnection();
    }

    public static void testDatabaseConnection() {
        try {
            SqliteUserDAO Database = SqliteUserDAO.getInstance();
            if (Database.isConnected()) {
                System.out.println("Database connected successfully!");
            } else {
                System.out.println("Failed to connect to database");
            }
        } catch (Exception e) {
            System.out.println("Error testing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
