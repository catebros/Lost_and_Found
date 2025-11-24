package net.javaguids.lost_and_found.database;

import org.junit.jupiter.api.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

// Test class for DatabaseManager
// Tests the Singleton pattern implementation and database connection functionality
class DatabaseManagerTest {

    private static final String TEST_DB_FILE = "lostandfound.db";
    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
    }


    @Test
    @DisplayName("Test getInstance returns non-null instance")
    void testGetInstanceReturnsNonNull() {
        DatabaseManager instance = DatabaseManager.getInstance();
        assertNotNull(instance, "DatabaseManager instance should not be null");
    }

    @Test
    @DisplayName("Test Singleton pattern - getInstance always returns same instance")
    void testSingletonPattern() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        assertSame(instance1, instance2,
            "DatabaseManager should return the same instance (Singleton pattern)");
    }

    @Test
    @DisplayName("Test getConnection returns non-null connection")
    void testGetConnectionReturnsNonNull() {
        Connection connection = dbManager.getConnection();
        assertNotNull(connection, "Connection should not be null");
    }

    @Test
    @DisplayName("Test connection is valid and open")
    void testConnectionIsValid() throws SQLException {
        Connection connection = dbManager.getConnection();
        assertNotNull(connection, "Connection should not be null");
        assertFalse(connection.isClosed(), "Connection should be open");
        assertTrue(connection.isValid(2), "Connection should be valid");
    }

    @Test
    @DisplayName("Test database file exists")
    void testDatabaseFileExists() {
        File dbFile = new File(TEST_DB_FILE);
        assertTrue(dbFile.exists(),
            "Database file should exist at: " + dbFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Test same connection is returned on multiple calls")
    void testSameConnectionReturned() {
        Connection conn1 = dbManager.getConnection();
        Connection conn2 = dbManager.getConnection();
        assertSame(conn1, conn2,
            "Should return the same connection instance");
    }

    @Test
    @DisplayName("Test closeConnection closes the connection")
    void testCloseConnection() throws SQLException {
        // Create a new instance for this test to avoid affecting other tests
        Connection connection = dbManager.getConnection();
        dbManager.closeConnection();
        assertTrue(connection.isClosed(),
            "Connection should be closed after calling closeConnection()");
    }
}
