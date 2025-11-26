package net.javaguids.lost_and_found.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 algorithm for password security.
 * Note: In production, consider using BCrypt or Argon2 for better security
 * as SHA-256 alone is vulnerable to rainbow table attacks.
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using SHA-256 algorithm.
     * @param password The plain text password to hash
     * @return Base64 encoded hash of the password
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies if a plain text password matches a hashed password.
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }
}