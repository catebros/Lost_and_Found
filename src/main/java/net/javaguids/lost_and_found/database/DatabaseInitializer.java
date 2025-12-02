package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.utils.PasswordUtil;

import java.sql.*;
import java.util.UUID;

// Database initialization script that creates all necessary tables.
// Run this once before starting the application if the database doesn't exist.

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:sqlite:lostandfound.db";

    public static void main(String[] args) {
        System.out.println("INITIALIZING LOST AND FOUND DATABASE");

        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            createTables(connection);
            createDefaultAdmin(connection);

            System.out.println("DATABASE INITIALIZATION COMPLETED SUCCESSFULLY");
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to initialize database");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        System.out.println("Creating tables...");

        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id TEXT PRIMARY KEY," +
                    "username TEXT UNIQUE NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "role TEXT NOT NULL," +
                    "created_at TEXT NOT NULL" +
                    ")");
            System.out.println("Created 'users' table");

            // Create items table
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "item_id TEXT PRIMARY KEY," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "category TEXT," +
                    "location TEXT," +
                    "date_posted TEXT NOT NULL," +
                    "status TEXT NOT NULL," +
                    "posted_by_user_id TEXT NOT NULL," +
                    "image_path TEXT," +
                    "type TEXT NOT NULL," +
                    "date_lost_found TEXT," +
                    "reward REAL," +
                    "FOREIGN KEY (posted_by_user_id) REFERENCES users(user_id)" +
                    ")");
            System.out.println("Created 'items' table");

            // Create messages table
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id TEXT PRIMARY KEY," +
                    "sender_id TEXT NOT NULL," +
                    "receiver_id TEXT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "FOREIGN KEY (sender_id) REFERENCES users(user_id)," +
                    "FOREIGN KEY (receiver_id) REFERENCES users(user_id)" +
                    ")");
            System.out.println("Created 'messages' table");

            // Create activity_logs table
            stmt.execute("CREATE TABLE IF NOT EXISTS activity_logs (" +
                    "log_id TEXT PRIMARY KEY," +
                    "user_id TEXT NOT NULL," +
                    "action TEXT NOT NULL," +
                    "details TEXT," +
                    "timestamp TEXT NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                    ")");
            System.out.println("Created 'activity_logs' table");
        }
    }

    private static void createDefaultAdmin(Connection connection) throws SQLException {
        System.out.println("\nChecking for default admin user...");

        String checkQuery = "SELECT COUNT(*) as count FROM users WHERE role = 'ADMIN'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {

            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("No admin found. Creating default admin user...");

                String adminId = UUID.randomUUID().toString();
                String username = "admin";
                String email = "admin@lostandfound.com";
                String password = "admin123";
                String passwordHash = PasswordUtil.hashPassword(password);

                String insertQuery = "INSERT INTO users (user_id, username, email, password_hash, role, created_at) " +
                        "VALUES (?, ?, ?, ?, 'ADMIN', ?)";

                try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                    pstmt.setString(1, adminId);
                    pstmt.setString(2, username);
                    pstmt.setString(3, email);
                    pstmt.setString(4, passwordHash);
                    pstmt.setString(5, java.time.LocalDateTime.now().toString());
                    pstmt.executeUpdate();
                }

                System.out.println("DEFAULT ADMIN USER CREATED");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
                System.out.println("Email: admin@lostandfound.com");
            } else {
                System.out.println("Admin user already exists");
            }
        }
    }
}