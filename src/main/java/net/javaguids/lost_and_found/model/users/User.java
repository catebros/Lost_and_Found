package net.javaguids.lost_and_found.model.users;
import java.lang.String;
import net.javaguids.lost_and_found.model.enums.UserRole;
import java.time.LocalDateTime;

// Base class for all user types
public abstract class User {
    protected String userId; // Unique identifier for the user
    protected String username; // User's login name
    protected String email; // User's email address
    protected String passwordHash; // Hashed password for security
    protected UserRole role; // User's role (ADMIN, MODERATOR, USER)
    protected LocalDateTime createdAt; // Account creation timestamp

    // Constructor that initializes a user with their basic information
    public User(String userId, String username, String email, String passwordHash, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Abstract method for displaying user-specific dashboard
    public abstract void displayDashboard();

    // Getters and setters for user attributes
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}