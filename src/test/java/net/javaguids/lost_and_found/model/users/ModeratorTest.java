package net.javaguids.lost_and_found.model.users;

import net.javaguids.lost_and_found.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ModeratorTest {

    private Moderator moderator;

    @BeforeEach
    void setUp() {
        moderator = new Moderator("mod-001", "moderatorUser", "moderator@example.com", "hashedPassword123");
    }

    @Test
    @DisplayName("Moderator constructor initializes all fields correctly")
    void testModeratorConstructor() {
        assertEquals("mod-001", moderator.getUserId());
        assertEquals("moderatorUser", moderator.getUsername());
        assertEquals("moderator@example.com", moderator.getEmail());
        assertEquals("hashedPassword123", moderator.getPasswordHash());
        assertEquals(UserRole.MODERATOR, moderator.getRole());
        assertNotNull(moderator.getCreatedAt());
    }

    @Test
    @DisplayName("Moderator has MODERATOR role")
    void testModeratorHasModeratorRole() {
        assertEquals(UserRole.MODERATOR, moderator.getRole());
    }

    @Test
    @DisplayName("Moderator username can be updated")
    void testSetUsername() {
        moderator.setUsername("newModeratorName");
        assertEquals("newModeratorName", moderator.getUsername());
    }

    @Test
    @DisplayName("Moderator email can be updated")
    void testSetEmail() {
        moderator.setEmail("newmod@example.com");
        assertEquals("newmod@example.com", moderator.getEmail());
    }

    @Test
    @DisplayName("Moderator password hash can be updated")
    void testSetPasswordHash() {
        moderator.setPasswordHash("newHashedPassword456");
        assertEquals("newHashedPassword456", moderator.getPasswordHash());
    }

    @Test
    @DisplayName("Moderator displayDashboard executes without error")
    void testDisplayDashboard() {
        assertDoesNotThrow(() -> moderator.displayDashboard());
    }

    @Test
    @DisplayName("Moderator userId is immutable")
    void testUserIdIsImmutable() {
        String originalId = moderator.getUserId();
        assertEquals("mod-001", originalId);
    }

    @Test
    @DisplayName("Moderator createdAt timestamp is set")
    void testCreatedAtTimestamp() {
        assertNotNull(moderator.getCreatedAt());
        assertTrue(moderator.getCreatedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Moderator has elevated permissions compared to regular users")
    void testModeratorRoleElevation() {
        RegularUser regularUser = new RegularUser("user-001", "john", "john@example.com", "hash");
        assertNotEquals(moderator.getRole(), regularUser.getRole());
        assertEquals(UserRole.MODERATOR, moderator.getRole());
        assertEquals(UserRole.USER, regularUser.getRole());
    }
}
