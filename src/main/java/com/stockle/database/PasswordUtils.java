package com.stockle.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords using BCrypt.
 * Centralises password security logic so no other class needs to import BCrypt directly
 */
public class PasswordUtils {

    /**
     * Hashes a plain text password using BCrypt
     * @param password the plain text password
     * @return a hashed string safe for storing inside the database
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifies a plain text password against a stored BCrypt Hash
     * @param password the plain text password to check
     * @param storedHash the hash retrieved from the database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

}

