package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;

public class User_DB_Test {

    private static SQLUserDAO userDAO;

    private static int randomId;

    @BeforeEach
    public void setUp() {
        userDAO = SQLUserDAO.getInstance();
        randomId = new Random().nextInt(SQLUserDAO.getInstance().getAllUsers().size()) + 1;
    }

    @Test
    public void testUserName() {
        String username = "TestUsername" + new Random().nextInt(100000);
        User user = new User(username, "TestPassword", "test@example.com", "Test User", LocalDate.now());
        assertEquals(username, user.getUsername());
    }

    @Test
    public void testUserEmail() {
        String email = "test"+new Random().nextInt(100000)+"@example.com";
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", email, "Test User", LocalDate.now());
        assertEquals(email, user.getEmail());
    }

    @Test 
    public void testUserID() {
        java.util.List<User> users = userDAO.getAllUsers();
        long totalUserID = users.size();
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test User", LocalDate.now());
        userDAO.addUser(user);
        assertEquals(totalUserID + 1, user.getId());
    }

    @Test
    public void testUserBalance() {
        long initialBalance = 100000;
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test User", LocalDate.now());
        assertEquals(initialBalance, user.getBalance());
    }

    @Test
    public void testUserProfit() {
        long initialProfit = 0;
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test User", LocalDate.now());
        assertEquals(initialProfit, user.getTotalProfit());
    }
}
