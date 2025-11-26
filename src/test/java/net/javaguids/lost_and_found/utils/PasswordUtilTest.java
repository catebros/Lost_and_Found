package net.javaguids.lost_and_found.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PasswordUtil class.
 * Tests password hashing and verification functionality.
 */
class PasswordUtilTest {

    @Test
    void testHashPassword_NotNull() {
        String password = "testPassword123";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash, "Hash should not be null");
        assertFalse(hash.isEmpty(), "Hash should not be empty");
    }

    @Test
    void testHashPassword_Deterministic() {
        String password = "samePassword";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertEquals(hash1, hash2, "Same password should produce same hash");
    }

    @Test
    void testHashPassword_DifferentPasswords() {
        String password1 = "password1";
        String password2 = "password2";
        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testVerifyPassword_CorrectPassword() {
        String password = "mySecurePassword";
        String hash = PasswordUtil.hashPassword(password);

        assertTrue(PasswordUtil.verifyPassword(password, hash),
                "Verification should succeed for correct password");
    }

    @Test
    void testVerifyPassword_IncorrectPassword() {
        String password = "mySecurePassword";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtil.hashPassword(password);

        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hash),
                "Verification should fail for incorrect password");
    }

    @Test
    void testVerifyPassword_CaseSensitive() {
        String password = "MyPassword";
        String hash = PasswordUtil.hashPassword(password);

        assertFalse(PasswordUtil.verifyPassword("mypassword", hash),
                "Password verification should be case-sensitive");
    }
}