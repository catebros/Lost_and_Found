package net.javaguids.lost_and_found.utils;

import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Test suite for ValidationUtil class. Tests email, password, and item validation logic.
class ValidationUtilTest {

    // Email Validation Tests
    @Test
    void testIsValidEmail_ValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("test.user@domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user_name@example.com"));
    }

    @Test
    void testIsValidEmail_InvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
        assertFalse(ValidationUtil.isValidEmail("user @example.com"));
    }

    // Password Validation Tests
    @Test
    void testIsValidPassword_ValidPasswords() {
        assertTrue(ValidationUtil.isValidPassword("12345678"));
        assertTrue(ValidationUtil.isValidPassword("password"));
        assertTrue(ValidationUtil.isValidPassword("VeryLongPassword123!"));
    }

    @Test
    void testIsValidPassword_InvalidPasswords() {
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("short"));
        assertFalse(ValidationUtil.isValidPassword("1234567")); // 7 chars
    }

    @Test
    void testIsValidPassword_ExactlyMinimumLength() {
        assertTrue(ValidationUtil.isValidPassword("12345678"),
                "Exactly 8 characters should be valid");
    }
}