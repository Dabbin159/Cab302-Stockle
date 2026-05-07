package com.stockle.test;

import com.stockle.database.SQLUserDAO;
import com.stockle.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class LogIn_Test {

    @Test
    void loginWithValidEmailAndPasswordReturnsUser() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        dao.signup("testuser", "TestPassword123", "test@example.com",
                "Test User", null);
        User result = dao.login("test@example.com", "TestPassword123");
        assertNotNull(result);
    }

    @Test
    void loginWithWrongPasswordReturnsNull() {
        SQLUserDAO dao = SQLUserDAO.getInstance();
        dao.signup("testuser2", "Testpasteword123", "test2@example.com"
        , "Test User", null);
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
