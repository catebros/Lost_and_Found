package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.utils.PasswordUtil;
import net.javaguids.lost_and_found.utils.ValidationUtil;
// TODO: Uncomment when ActivityLog is implemented
// import net.javaguids.java_final_project.analytics.ActivityLog;
import net.javaguids.lost_and_found.exceptions.AuthException;

import java.util.UUID;

/**
 * Service class for user authentication and registration.
 * Manages user login, registration, logout, and session tracking.
 * Uses singleton pattern to maintain current user session.
 */
public class AuthService {

    /** Currently logged-in user (null if no user is logged in) */
    private static User currentUser;

    /**
     * Authenticates a user with username and password.
     * @param username The username to authenticate
     * @param password The plain text password
     * @return The authenticated User object if successful, null otherwise
     */
    public static User login(String username, String password) {
        UserRepository userRepo = UserRepository.getInstance();
        User user = userRepo.getUserByUsername(username);

        if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            currentUser = user;
            // TODO: Uncomment when ActivityLog is implemented
            // ActivityLog.log(user.getUserId(), "LOGIN", "User logged in");
            return user;
        }

        return null;
    }

    /**
     * Registers a new user account with validation.
     * @param username The desired username (must be unique and non-empty)
     * @param email The user's email address (must be valid format)
     * @param password The password (must be at least 8 characters)
     * @return The newly created User object
     * @throws AuthException if validation fails or username already exists
     */
    public static User register(String username, String email, String password) throws AuthException {
        // Validate username
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username cannot be empty");
        }

        // Validate email format
        if (email == null || !ValidationUtil.isValidEmail(email)) {
            throw new AuthException("Invalid email address");
        }

        // Validate password strength
        if (password == null || !ValidationUtil.isValidPassword(password)) {
            throw new AuthException("Password must be at least 8 characters long");
        }

        UserRepository userRepo = UserRepository.getInstance();

        // Check if username already exists
        if (userRepo.getUserByUsername(username) != null) {
            throw new AuthException("Username already exists");
        }

        // Create new user with hashed password
        String userId = UUID.randomUUID().toString();
        String passwordHash = PasswordUtil.hashPassword(password);

        RegularUser newUser = new RegularUser(userId, username, email, passwordHash);

        // Save user to database
        boolean success = userRepo.saveUser(newUser);

        if (!success) {
            throw new AuthException("Failed to create user account");
        }

        // TODO: Uncomment when ActivityLog is implemented
        // ActivityLog.log(userId, "REGISTER", "New user registered: " + username);

        return newUser;
    }

    /**
     * Logs out the current user and clears the session.
     */
    public static void logout() {
        if (currentUser != null) {
            // TODO: Uncomment when ActivityLog is implemented
            // ActivityLog.log(currentUser.getUserId(), "LOGOUT", "User logged out");
            currentUser = null;
        }
    }

    /**
     * Gets the currently logged-in user.
     * @return The current User object, or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if the current user has admin privileges.
     * @return true if current user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    /**
     * Checks if a user is currently authenticated.
     * @return true if a user is logged in, false otherwise
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}