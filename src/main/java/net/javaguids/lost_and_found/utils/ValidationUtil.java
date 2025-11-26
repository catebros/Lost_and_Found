package net.javaguids.lost_and_found.utils;

import net.javaguids.lost_and_found.model.items.Item;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input and data integrity.
 * Provides validation for emails, passwords, and item data.
 */
public class ValidationUtil {

    /** Regular expression pattern for email validation */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /** Minimum required password length */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validates email format using regex pattern.
     * @param email The email address to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates password meets minimum length requirement.
     * @param password The password to validate
     * @return true if password is at least 8 characters, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Validates that an item has all required fields populated.
     * Checks that title, description, category, and location are not null or empty.
     * @param item The item to validate
     * @return true if all required fields are valid, false otherwise
     */
    public static boolean isValidItem(Item item) {
        if (item == null) {
            return false;
        }
        return item.getTitle() != null && !item.getTitle().trim().isEmpty() &&
                item.getDescription() != null && !item.getDescription().trim().isEmpty() &&
                item.getCategory() != null && !item.getCategory().trim().isEmpty() &&
                item.getLocation() != null && !item.getLocation().trim().isEmpty();
    }
}