package com.stockle.test;

import com.stockle.database.PasswordUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtils_Test {

    /**
     * Verifies that hashing a password returns a non-null result.
     */
    @Test
    void hashPasswordReturnsNonNullHash() {
        String hash = PasswordUtils.hashPassword("mypassword");
        assertNotNull(hash);
    }

    /**
     * Verifies that the correct password matches its hash.
     */
    @Test
    void verifyPasswordReturnsTrueForCorrectPassword() {
        String hash = PasswordUtils.hashPassword("mypassword");
        assertTrue(PasswordUtils.verifyPassword("mypassword",
                hash));
    }

    /**
     * Verifies that a wrong does not match the hash.
     */
    @Test
    void verifyPasswordReturnsFalseForWrongPassword() {
        String hash = PasswordUtils.hashPassword("mypassword");
        assertFalse(PasswordUtils.verifyPassword(
                "wrongpassword", hash));
    }
}
