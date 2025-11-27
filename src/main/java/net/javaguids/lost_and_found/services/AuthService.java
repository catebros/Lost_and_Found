package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.utils.PasswordUtil;
import net.javaguids.lost_and_found.utils.ValidationUtil;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import net.javaguids.lost_and_found.exceptions.AuthException;

import java.util.UUID;

// Service class for user authentication and registration. Manages user login, registration, logout, and session tracking.
public class AuthService {

    // Currently logged-in user (null if no user is logged in)
    private static User currentUser;

    // Authenticates a user with username and password.
    public static User login(String username, String password) {
        UserRepository userRepo = UserRepository.getInstance();
        User user = userRepo.getUserByUsername(username);

        if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            currentUser = user;
            ActivityLog.log(user.getUserId(), "LOGIN", "User logged in");
            return user;
        }

        return null;
    }

    // Registers a new user account with validation.
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

        ActivityLog.log(userId, "REGISTER", "New user registered: " + username);
        return newUser;
    }

    // Logs out the current user and clears the session
    public static void logout() {
        if (currentUser != null) {
            ActivityLog.log(currentUser.getUserId(), "LOGOUT", "User logged out");
            currentUser = null;
        }
    }

    // Gets the currently logged-in user.
    public static User getCurrentUser() {
        return currentUser;
    }

    // Checks if the current user has admin privileges.
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    // Checks if a user is currently authenticated.
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}