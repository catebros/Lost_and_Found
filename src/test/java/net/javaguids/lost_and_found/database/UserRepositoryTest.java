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
    @DisplayName("Test saveUser saves a new user successfully")
    void testSaveUser() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");

        // Act
        boolean result = repository.saveUser(testUser);

        // Assert
        assertTrue(result, "saveUser should return true on success");
        
        // Verify user was saved by retrieving it
        User savedUser = repository.getUserById(testUserId);
        assertNotNull(savedUser, "Saved user should be retrievable");
        assertEquals(testUsername, savedUser.getUsername());
        assertEquals(testEmail, savedUser.getEmail());
        assertEquals(UserRole.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Test getUserById retrieves existing user")
    void testGetUserById_UserExists() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(testUser);

        // Act
        User retrievedUser = repository.getUserById(testUserId);

        // Assert
        assertNotNull(retrievedUser, "User should be found");
        assertEquals(testUserId, retrievedUser.getUserId());
        assertEquals(testUsername, retrievedUser.getUsername());
        assertEquals(testEmail, retrievedUser.getEmail());
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
    @DisplayName("Test getUserByUsername retrieves existing user")
    void testGetUserByUsername_UserExists() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(testUser);

        // Act
        User retrievedUser = repository.getUserByUsername(testUsername);

        // Assert
        assertNotNull(retrievedUser, "User should be found by username");
        assertEquals(testUsername, retrievedUser.getUsername());
        assertEquals(testUserId, retrievedUser.getUserId());
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
    @DisplayName("Test getUserByEmail retrieves existing user")
    void testGetUserByEmail_UserExists() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(testUser);

        // Act
        User retrievedUser = repository.getUserByEmail(testEmail);

        // Assert
        assertNotNull(retrievedUser, "User should be found by email");
        assertEquals(testEmail, retrievedUser.getEmail());
        assertEquals(testUserId, retrievedUser.getUserId());
    }

    @Test
    @DisplayName("Test getUserByEmail returns null for non-existent email")
    void testGetUserByEmail_UserNotFound() {
        // Act
        User retrievedUser = repository.getUserByEmail("nonexistent@test.com");

        // Assert
        assertNull(retrievedUser, "Should return null for non-existent email");
    }

    @Test
    @DisplayName("Test updateUser updates existing user information")
    void testUpdateUser() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(testUser);

        // Update user information
        testUser.setUsername("updatedUsername");
        testUser.setEmail("updated@test.com");
        testUser.setPasswordHash("newHashedPassword456");

        // Act
        boolean result = repository.updateUser(testUser);

        // Assert
        assertTrue(result, "updateUser should return true on success");
        
        // Verify updates were saved
        User updatedUser = repository.getUserById(testUserId);
        assertNotNull(updatedUser);
        assertEquals("updatedUsername", updatedUser.getUsername());
        assertEquals("updated@test.com", updatedUser.getEmail());
        assertEquals("newHashedPassword456", updatedUser.getPasswordHash());
    }

    @Test
    @DisplayName("Test deleteUser removes user from database")
    void testDeleteUser() {
        // Arrange
        User testUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(testUser);

        // Verify user exists
        User beforeDelete = repository.getUserById(testUserId);
        assertNotNull(beforeDelete, "User should exist before deletion");

        // Act
        boolean result = repository.deleteUser(testUserId);

        // Assert
        assertTrue(result, "deleteUser should return true on success");
        
        // Verify user no longer exists
        User afterDelete = repository.getUserById(testUserId);
        assertNull(afterDelete, "User should not exist after deletion");
    }

    @Test
    @DisplayName("Test getAllUsers returns list of all users")
    void testGetAllUsers() {
        // Arrange - create multiple test users
        String userId1 = "test-" + UUID.randomUUID().toString().substring(0, 8);
        String userId2 = "test-" + UUID.randomUUID().toString().substring(0, 8);
        
        User user1 = new RegularUser(userId1, "user1-" + UUID.randomUUID().toString().substring(0, 8),
            "user1@test.com", "hash1");
        User user2 = new RegularUser(userId2, "user2-" + UUID.randomUUID().toString().substring(0, 8),
            "user2@test.com", "hash2");
        
        repository.saveUser(user1);
        repository.saveUser(user2);

        try {
            // Act
            List<User> allUsers = repository.getAllUsers();

            // Assert
            assertNotNull(allUsers, "getAllUsers should return a non-null list");
            assertTrue(allUsers.size() >= 2, "Should return at least the two test users");
            
            // Verify our test users are in the list
            boolean foundUser1 = allUsers.stream()
                .anyMatch(u -> u.getUserId().equals(userId1));
            boolean foundUser2 = allUsers.stream()
                .anyMatch(u -> u.getUserId().equals(userId2));
            
            assertTrue(foundUser1, "User1 should be in the list");
            assertTrue(foundUser2, "User2 should be in the list");
        } finally {
            // Cleanup
            repository.deleteUser(userId1);
            repository.deleteUser(userId2);
        }
    }

    @Test
    @DisplayName("Test extractUserFromResultSet creates Admin for ADMIN role")
    void testExtractUserFromResultSet_CreatesAdmin() {
        // Arrange
        User adminUser = new Admin(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(adminUser);

        // Act
        User retrievedUser = repository.getUserById(testUserId);

        // Assert
        assertNotNull(retrievedUser);
        assertTrue(retrievedUser instanceof Admin, "Should be an Admin instance");
        assertEquals(UserRole.ADMIN, retrievedUser.getRole());
    }

    @Test
    @DisplayName("Test extractUserFromResultSet creates Moderator for MODERATOR role")
    void testExtractUserFromResultSet_CreatesModerator() {
        // Arrange
        User moderatorUser = new Moderator(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(moderatorUser);

        // Act
        User retrievedUser = repository.getUserById(testUserId);

        // Assert
        assertNotNull(retrievedUser);
        assertTrue(retrievedUser instanceof Moderator, "Should be a Moderator instance");
        assertEquals(UserRole.MODERATOR, retrievedUser.getRole());
    }

    @Test
    @DisplayName("Test extractUserFromResultSet creates RegularUser for USER role")
    void testExtractUserFromResultSet_CreatesRegularUser() {
        // Arrange
        User regularUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(regularUser);

        // Act
        User retrievedUser = repository.getUserById(testUserId);

        // Assert
        assertNotNull(retrievedUser);
        assertTrue(retrievedUser instanceof RegularUser, "Should be a RegularUser instance");
        assertEquals(UserRole.USER, retrievedUser.getRole());
    }

    @Test
    @DisplayName("Test updateUser handles role change from USER to MODERATOR")
    void testUpdateUser_RoleChangeToModerator() {
        // Arrange
        User regularUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(regularUser);
        
        // Change role to Moderator
        User moderatorUser = new Moderator(testUserId, testUsername, testEmail, "hashedPassword123");

        // Act
        boolean result = repository.updateUser(moderatorUser);

        // Assert
        assertTrue(result, "updateUser should succeed");
        User updatedUser = repository.getUserById(testUserId);
        assertNotNull(updatedUser);
        assertEquals(UserRole.MODERATOR, updatedUser.getRole());
        assertTrue(updatedUser instanceof Moderator);
    }

    @Test
    @DisplayName("Test updateUser handles role change from USER to ADMIN")
    void testUpdateUser_RoleChangeToAdmin() {
        // Arrange
        User regularUser = new RegularUser(testUserId, testUsername, testEmail, "hashedPassword123");
        repository.saveUser(regularUser);
        
        // Change role to Admin
        User adminUser = new Admin(testUserId, testUsername, testEmail, "hashedPassword123");

        // Act
        boolean result = repository.updateUser(adminUser);

        // Assert
        assertTrue(result, "updateUser should succeed");
        User updatedUser = repository.getUserById(testUserId);
        assertNotNull(updatedUser);
        assertEquals(UserRole.ADMIN, updatedUser.getRole());
        assertTrue(updatedUser instanceof Admin);
    }
}

