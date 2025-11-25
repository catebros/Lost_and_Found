package net.javaguids.lost_and_found.model.users;

import net.javaguids.lost_and_found.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin("admin-001", "adminUser", "admin@example.com", "hashedPassword123");
    }

    @Test
    @DisplayName("Admin constructor initializes all fields correctly")
    void testAdminConstructor() {
        assertEquals("admin-001", admin.getUserId());
        assertEquals("adminUser", admin.getUsername());
        assertEquals("admin@example.com", admin.getEmail());
        assertEquals("hashedPassword123", admin.getPasswordHash());
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertNotNull(admin.getCreatedAt());
    }

    @Test
    @DisplayName("Admin has ADMIN role")
    void testAdminHasAdminRole() {
        assertEquals(UserRole.ADMIN, admin.getRole());
    }

    @Test
    @DisplayName("Admin username can be updated")
    void testSetUsername() {
        admin.setUsername("newAdminName");
        assertEquals("newAdminName", admin.getUsername());
    }

    @Test
    @DisplayName("Admin email can be updated")
    void testSetEmail() {
        admin.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", admin.getEmail());
    }

    @Test
    @DisplayName("Admin password hash can be updated")
    void testSetPasswordHash() {
        admin.setPasswordHash("newHashedPassword456");
        assertEquals("newHashedPassword456", admin.getPasswordHash());
    }

    @Test
    @DisplayName("Admin displayDashboard executes without error")
    void testDisplayDashboard() {
        assertDoesNotThrow(() -> admin.displayDashboard());
    }

    @Test
    @DisplayName("Admin userId is immutable")
    void testUserIdIsImmutable() {
        String originalId = admin.getUserId();
        assertEquals("admin-001", originalId);
    }

    @Test
    @DisplayName("Admin createdAt timestamp is set")
    void testCreatedAtTimestamp() {
        assertNotNull(admin.getCreatedAt());
        assertTrue(admin.getCreatedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }
}
