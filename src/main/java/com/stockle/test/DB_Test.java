package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

public class DB_Test {

    public static void main(String[] args) {
        AddUserTest();
        int randomId = new Random().nextInt(SQLUserDAO.getInstance().getAllUsers().size()) + 1; // Get a random user ID from the database (Selects Random User)
        GetUserByIdTest(randomId);
        UpdateUserTestValidate(randomId);
        UpdateUserTest(randomId);
        UpdateUserTestValidate(randomId);
        // UniqueUserTest();
    }

    public static void AddUserTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test", "User",LocalDate.now());
        userDAO.addUser(user);
        System.out.println("User added with ID: " + user.getId());
    }

    public static void GetUserByIdTest(int id) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(id);
        if (user != null) {
            System.out.println("User found: " + user.getUsername());
        } else {
            System.out.println("User not found.");
        }
    }

    public static void UpdateUserTest(int id) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(id);
        if (user != null) {
            user.setEmail("updated" + new Random().nextInt(100000) + "@example.com");
            userDAO.updateUser(user);
            System.out.println("User updated.");
        } else {
            System.out.println("User not found.");
        }
    }

    public static void UpdateUserTestValidate(int id) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(id);
        if (user != null) {
            System.out.println("User found: " + user.getEmail());
        } else {
            System.out.println("User not found.");
        }
    }

    public static void UniqueUserTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user1 = new User("UniqueUsername", "TestPassword", "test1@example.com", "Test", "User", LocalDate.now());
        User user2 = new User("UniqueUsername", "TestPassword", "test2@example.com", "Test", "User", LocalDate.now());
        try {
            userDAO.addUser(user1);
            System.out.println("First user added with ID: " + user1.getId());
        } catch (Exception e) {
            System.out.println("Failed to add first user: " + e.getMessage());
        }
        try {
            userDAO.addUser(user2);
            System.out.println("Second user added with ID: This should not happen");
        } catch (Exception e) {
            System.out.println("Failed to add second user: This is good unique constraint is working");
        }
    }
}