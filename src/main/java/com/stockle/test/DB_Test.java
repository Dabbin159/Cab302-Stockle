package com.stockle.test;

import java.time.LocalDate;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

public class DB_Test {

    public static void main(String[] args) {
        AddUserTest();
        GetUserByIdTest();
        UpdateUserTestValidate();
        UpdateUserTest();
        UpdateUserTestValidate();
    }

    public static void AddUserTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = new User("TestUsername", "TestPassword", "test@example.com", "Test", "User",LocalDate.now());
        userDAO.addUser(user);
        System.out.println("User added with ID: " + user.getId());
    }

    public static void GetUserByIdTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(1);
        if (user != null) {
            System.out.println("User found: " + user.getUsername());
        } else {
            System.out.println("User not found.");
        }
    }

    public static void UpdateUserTest() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(1);
        if (user != null) {
            user.setEmail("updated@example.com");
            userDAO.updateUser(user);
            System.out.println("User updated.");
        } else {
            System.out.println("User not found.");
        }
    }

    public static void UpdateUserTestValidate() {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = userDAO.getUserById(1);
        if (user != null) {
            System.out.println("User found: " + user.getEmail());
        } else {
            System.out.println("User not found.");
        }
    }
}