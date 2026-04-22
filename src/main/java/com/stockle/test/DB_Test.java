package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

public class DB_Test {

    public static void main(String[] args) {
        AddUserTest();
        int randomId = new Random().nextInt(SQLUserDAO.getInstance().getAllUsers().size()) + 1; // Get a random user ID from the database
        GetUserByIdTest(randomId);
        UpdateUserTestValidate(randomId);
        UpdateUserTest(randomId);
        UpdateUserTestValidate(randomId);
    }

    public static void AddUserTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = new User("TestUsername", "TestPassword", "test@example.com", "Test", "User",LocalDate.now());
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
            user.setEmail("updated@example.com");
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
}