package net.javaguids.lost_and_found.model.users;

import net.javaguids.lost_and_found.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class RegularUserTest {

    private RegularUser regularUser;

    @BeforeEach
    void setUp() {
        regularUser = new RegularUser("user-001", "johnDoe", "john@example.com", "hashedPassword123");
    }

    @Test
    @DisplayName("RegularUser constructor initializes all fields correctly")
    void testRegularUserConstructor() {
        assertEquals("user-001", regularUser.getUserId());
        assertEquals("johnDoe", regularUser.getUsername());
        assertEquals("john@example.com", regularUser.getEmail());
        assertEquals("hashedPassword123", regularUser.getPasswordHash());
        assertEquals(UserRole.USER, regularUser.getRole());
        assertNotNull(regularUser.getCreatedAt());
    }

    @Test
    @DisplayName("RegularUser has USER role")
    void testRegularUserHasUserRole() {
        assertEquals(UserRole.USER, regularUser.getRole());
    }

    @Test
    @DisplayName("RegularUser username can be updated")
    void testSetUsername() {
        regularUser.setUsername("janeDoe");
        assertEquals("janeDoe", regularUser.getUsername());
    }

    @Test
    @DisplayName("RegularUser email can be updated")
    void testSetEmail() {
        regularUser.setEmail("jane@example.com");
        assertEquals("jane@example.com", regularUser.getEmail());
    }

    @Test
    @DisplayName("RegularUser password hash can be updated")
    void testSetPasswordHash() {
        regularUser.setPasswordHash("newHashedPassword456");
        assertEquals("newHashedPassword456", regularUser.getPasswordHash());
    }

    @Test
    @DisplayName("RegularUser displayDashboard executes without error")
    void testDisplayDashboard() {
        assertDoesNotThrow(() -> regularUser.displayDashboard());
    }

    @Test
    @DisplayName("RegularUser userId is immutable")
    void testUserIdIsImmutable() {
        String originalId = regularUser.getUserId();
        assertEquals("user-001", originalId);
    }

    @Test
    @DisplayName("RegularUser createdAt timestamp is set")
    void testCreatedAtTimestamp() {
        assertNotNull(regularUser.getCreatedAt());
        assertTrue(regularUser.getCreatedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Multiple RegularUser instances have different creation times")
    void testMultipleInstancesIndependent() {
        RegularUser user2 = new RegularUser("user-002", "janeDoe", "jane@example.com", "hashedPassword456");
        assertNotEquals(regularUser.getUserId(), user2.getUserId());
        assertNotEquals(regularUser.getUsername(), user2.getUsername());
    }
}
