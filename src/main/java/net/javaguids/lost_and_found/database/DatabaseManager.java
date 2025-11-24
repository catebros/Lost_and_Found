package net.javaguids.lost_and_found.database;

import java.io.File;
import java.sql.*;

// DatabaseManager handles the database connection for the Lost and Found application.
// This class implements the Singleton pattern to ensure only one database connection exists.
//
// IMPORTANT: Before running the application for the first time, you must initialize
// the database.
//
// The database file is expected to be at: lostandfound.db
public class DatabaseManager {
    // Singleton instance of DatabaseManager
    private static DatabaseManager instance;
    // SQLite database connection
    private Connection connection;
    // Database URL for JDBC connection
    private static final String DB_URL = "jdbc:sqlite:lostandfound.db";
    // Database file name
    private static final String DB_FILE = "lostandfound.db";

    // Private constructor to prevent external instantiation (Singleton pattern)
    private DatabaseManager() {
        try {
            // Check if database file exists
            File dbFile = new File(DB_FILE);
            if (!dbFile.exists()) {
                System.err.println("ERROR: Database file not found!");
                System.err.println("Please run DatabaseInitializer first to create the database.");
                System.err.println("Location: " + dbFile.getAbsolutePath());
                throw new RuntimeException("Database not initialized. Run DatabaseInitializer.main() first.");
            }

            // Connect to existing database
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to database: " + DB_FILE);

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to connect to database");
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
    }

    // Returns the singleton instance of DatabaseManager
    // Creates a new instance if one doesn't exist
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Returns the active database connection
    public Connection getConnection() {
        return connection;
    }

    // Closes the database connection.
    // Should be called when the application shuts down.
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close database connection");
                e.printStackTrace();
            }
        }
    }
}