package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing User entities in the database.
 * Implements the Singleton pattern to ensure only one instance exists.
 * Provides CRUD (Create, Read, Update, Delete) operations for users.
 * 
 * This class handles all database interactions related to users, including
 * creating different user types (Admin, Moderator, RegularUser) based on their role.
 */
public class UserRepository {
    // Singleton instance - only one UserRepository exists in the application
    private static UserRepository instance;
    // Database connection obtained from DatabaseManager
    private final Connection connection;

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes the database connection through DatabaseManager.
     */
    private UserRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Returns the singleton instance of UserRepository.
     * Creates a new instance if one doesn't exist (lazy initialization).
     * 
     * @return The single instance of UserRepository
     */
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Retrieves a user from the database by their unique user ID.
     * 
     * @param userId The unique identifier of the user to retrieve
     * @return User object if found, null if user doesn't exist or query fails
     */
    public User getUserById(String userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user from the database by their username.
     * Used primarily for login operations where users authenticate with their username.
     * 
     * @param username The username to search for (case-sensitive)
     * @return User object if found, null if user doesn't exist or query fails
     */
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user from the database by their email address.
     * Useful for registration validation to check if email is already in use.
     * 
     * @param email The email address to search for
     * @return User object if found, null if user doesn't exist or query fails
     */
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves a new user to the database.
     * Inserts a new user record with all provided information.
     * 
     * @param user The User object containing all user information to save
     * @return true if user was successfully saved, false if database operation failed
     */
    public boolean saveUser(User user) {
        String query = "INSERT INTO users (user_id, username, email, password_hash, role, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().toString());
            pstmt.setString(6, user.getCreatedAt().toString());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing user's information in the database.
     * 
     * Special handling: If a regular user is being promoted to Moderator or Admin,
     * their items and messages are automatically deleted as part of the role change.
     * This ensures moderators/admins don't have personal items or messages.
     * 
     * Uses database transactions to ensure data consistency when role changes occur.
     * 
     * @param user The User object with updated information
     * @return true if user was successfully updated, false if database operation failed
     */
    public boolean updateUser(User user) {
        try {
            // Get the old user to check if role changed
            User oldUser = getUserById(user.getUserId());
            // Check if user is being promoted from USER to MODERATOR or ADMIN
            boolean roleChanged = oldUser != null &&
                                 oldUser.getRole() == UserRole.USER &&
                                 (user.getRole() == UserRole.MODERATOR || user.getRole() == UserRole.ADMIN);

            // Start transaction if role changed to moderator/admin
            // This ensures all deletions happen atomically (all or nothing)
            if (roleChanged) {
                connection.setAutoCommit(false);

                // Delete user's items when promoted to moderator/admin
                // Moderators and admins shouldn't have personal lost/found items
                String deleteItems = "DELETE FROM items WHERE posted_by_user_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteItems)) {
                    pstmt.setString(1, user.getUserId());
                    pstmt.executeUpdate();
                }

                // Delete user's messages when promoted to moderator/admin
                // Clean up personal messages when role changes
                String deleteMessages = "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteMessages)) {
                    pstmt.setString(1, user.getUserId());
                    pstmt.setString(2, user.getUserId());
                    pstmt.executeUpdate();
                }
            }

            // Update user information in the database
            String query = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getEmail());
                pstmt.setString(3, user.getPasswordHash());
                pstmt.setString(4, user.getRole().toString());
                pstmt.setString(5, user.getUserId());

                pstmt.executeUpdate();
            }

            // Commit transaction if role changed (saves all changes atomically)
            if (roleChanged) {
                connection.commit();
                connection.setAutoCommit(true);
            }

            return true;
        } catch (SQLException e) {
            // Rollback transaction on error to maintain data consistency
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user from the database and all associated data.
     * 
     * This method performs a cascading delete:
     * 1. Deletes all messages sent or received by the user
     * 2. Deletes all items posted by the user
     * 3. Deletes all activity logs for the user
     * 4. Finally deletes the user record itself
     * 
     * Uses database transactions to ensure all deletions succeed or none do (atomicity).
     * 
     * @param userId The unique identifier of the user to delete
     * @return true if user was successfully deleted, false if database operation failed
     */
    public boolean deleteUser(String userId) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Delete user's messages (both sent and received)
            String deleteMessages = "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteMessages)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
            }

            // Delete user's items
            String deleteItems = "DELETE FROM items WHERE posted_by_user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteItems)) {
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
            }

            // Delete activity logs for this user
            String deleteLogs = "DELETE FROM activity_logs WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteLogs)) {
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
            }

            // Finally delete the user
            String deleteUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUser)) {
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
            }

            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all users from the database.
     * Primarily used by admin users to view and manage all system users.
     * 
     * @return List of all User objects in the database, empty list if no users exist or query fails
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Factory method that creates the appropriate User subclass based on the role stored in the database.
     * 
     * This method implements the Factory Pattern to instantiate the correct User type:
     * - Admin objects for ADMIN role
     * - Moderator objects for MODERATOR role
     * - RegularUser objects for USER role
     * 
     * @param rs The ResultSet containing user data from a database query
     * @return A User object of the appropriate subclass (Admin, Moderator, or RegularUser)
     * @throws SQLException If there's an error reading from the ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        // Extract all user fields from the ResultSet
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        // Convert role string to UserRole enum
        UserRole role = UserRole.valueOf(roleStr);

        // Create the appropriate User subclass based on role (Factory Pattern)
        if (role == UserRole.ADMIN) {
            return new Admin(userId, username, email, passwordHash);
        } else if (role == UserRole.MODERATOR) {
            return new Moderator(userId, username, email, passwordHash);
        } else {
            // Default to RegularUser for USER role
            return new RegularUser(userId, username, email, passwordHash);
        }
    }
}
