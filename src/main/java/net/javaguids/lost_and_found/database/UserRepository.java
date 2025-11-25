package net.javaguids.java_final_project.database;

import net.javaguids.java_final_project.model.users.User;
import net.javaguids.java_final_project.model.users.RegularUser;
import net.javaguids.java_final_project.model.users.Admin;
import net.javaguids.java_final_project.model.users.Moderator;
import net.javaguids.java_final_project.model.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static UserRepository instance;
    private final Connection connection;

    private UserRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

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

    public boolean updateUser(User user) {
        try {
            // Get the old user to check if role changed
            User oldUser = getUserById(user.getUserId());
            boolean roleChanged = oldUser != null &&
                                 oldUser.getRole() == UserRole.USER &&
                                 (user.getRole() == UserRole.MODERATOR || user.getRole() == UserRole.ADMIN);

            // Start transaction if role changed to moderator/admin
            if (roleChanged) {
                connection.setAutoCommit(false);

                // Delete user's items when promoted to moderator/admin
                String deleteItems = "DELETE FROM items WHERE posted_by_user_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteItems)) {
                    pstmt.setString(1, user.getUserId());
                    pstmt.executeUpdate();
                }

                // Delete user's messages when promoted to moderator/admin
                String deleteMessages = "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteMessages)) {
                    pstmt.setString(1, user.getUserId());
                    pstmt.setString(2, user.getUserId());
                    pstmt.executeUpdate();
                }
            }

            // Update user
            String query = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getEmail());
                pstmt.setString(3, user.getPasswordHash());
                pstmt.setString(4, user.getRole().toString());
                pstmt.setString(5, user.getUserId());

                pstmt.executeUpdate();
            }

            // Commit transaction if role changed
            if (roleChanged) {
                connection.commit();
                connection.setAutoCommit(true);
            }

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

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        UserRole role = UserRole.valueOf(roleStr);

        if (role == UserRole.ADMIN) {
            return new Admin(userId, username, email, passwordHash);
        } else if (role == UserRole.MODERATOR) {
            return new Moderator(userId, username, email, passwordHash);
        } else {
            return new RegularUser(userId, username, email, passwordHash);
        }
    }
}
