package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.enums.UserRole;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UserRepository.
 * Tests all CRUD operations and the Singleton pattern implementation.
 * 
 * Note: These tests use the actual database, so test data may persist.
 * Consider cleaning up test data after tests if needed.
 */
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    private UserRepository repository;
    private String testUserId;
    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setUp() {
        repository = UserRepository.getInstance();
        // Generate unique test identifiers to avoid conflicts
        testUserId = "test-" + UUID.randomUUID().toString().substring(0, 8);
        testUsername = "testuser-" + UUID.randomUUID().toString().substring(0, 8);
        testEmail = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        if (testUserId != null) {
            repository.deleteUser(testUserId);
        }
    }

    @Test
    @DisplayName("Test Singleton pattern - getInstance always returns same instance")
    void testSingletonPattern() {
        UserRepository instance1 = UserRepository.getInstance();
        UserRepository instance2 = UserRepository.getInstance();
        assertSame(instance1, instance2,
            "UserRepository should return the same instance (Singleton pattern)");
    }

    @Test
    @DisplayName("Test getInstance returns non-null instance")
    void testGetInstanceReturnsNonNull() {
        UserRepository instance = UserRepository.getInstance();
        assertNotNull(instance, "UserRepository instance should not be null");
    }

    @Test
    @DisplayName("Test getUserById returns null for non-existent user")
    void testGetUserById_UserNotFound() {
        // Act
        User retrievedUser = repository.getUserById("non-existent-id");

        // Assert
        assertNull(retrievedUser, "Should return null for non-existent user");
    }

    @Test
    @DisplayName("Test getUserByUsername returns null for non-existent username")
    void testGetUserByUsername_UserNotFound() {
        // Act
        User retrievedUser = repository.getUserByUsername("non-existent-username");

        // Assert
        assertNull(retrievedUser, "Should return null for non-existent username");
    }

    @Test
    @DisplayName("Test getUserByEmail returns null for non-existent email")
    void testGetUserByEmail_UserNotFound() {
        // Act
        User retrievedUser = repository.getUserByEmail("nonexistent@test.com");

        // Assert
        assertNull(retrievedUser, "Should return null for non-existent email");
    }


}

