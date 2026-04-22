package com.stockle.test;

import java.time.LocalDate;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

public class DB_Test {

    public static void main(String[] args) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = new User("TestUsername", "TestPassword", "test@example.com", "Test", "User",LocalDate.now());
        userDAO.addUser(user);
        System.out.println("User added with ID: " + user.getId());
    }
}
