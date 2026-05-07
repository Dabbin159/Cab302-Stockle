package com.stockle.test;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;


public class LogIn_Test {

    @BeforeEach
    void cleanUpTestUsers() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        dao.deleteUserByEmail("test@example.com");
        dao.deleteUserByEmail("test2@example.com");
    }

    @Test
    void loginWithValidEmailAndPasswordReturnsUser() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        dao.signup("testuser", "TestPassword123", "test@example.com",
                "Test User", LocalDate.of(2000, 1, 1));
        User result = dao.login("test@example.com", "TestPassword123");
        assertNotNull(result);
    }

    @Test
    void loginWithWrongPasswordReturnsNull() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        dao.signup("testuser2", "Testpasteword123", "test2@example.com"
        , "Test User", LocalDate.of(2000, 1, 1));
        User result = dao.login("test2@example.com", "WrongPassword");
        assertNull(result);
    }

    @Test
    void loginWithNonExistentEmailReturnsNull() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        User result = dao.login("nobody@example.com", "SomePassword");
        assertNull(result);
    }

}
