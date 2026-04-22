package com.stockle.test;

import com.stockle.database.SqliteConnectionDAO;

public class DB_Test {

    public static void main(String[] args) {
        testDatabaseConnection();
    }

    public static void testDatabaseConnection() {
        try {
            SqliteConnectionDAO Database = new SqliteConnectionDAO();
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
