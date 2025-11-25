package net.javaguids.lost_and_found.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AuthException.
 * Tests exception creation, message handling, and inheritance from LostAndFoundException.
 */
@DisplayName("AuthException Tests")
class AuthExceptionTest {

    @Test
    @DisplayName("Test AuthException can be created with a message")
    void testAuthExceptionCreation() {
        // Arrange & Act
        String errorMessage = "Invalid credentials";
        AuthException exception = new AuthException(errorMessage);

        // Assert
        assertNotNull(exception, "AuthException should be created");
        assertEquals(errorMessage, exception.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Test AuthException extends LostAndFoundException")
    void testAuthExceptionInheritance() {
        // Arrange & Act
        AuthException exception = new AuthException("Test message");

        // Assert
        assertTrue(exception instanceof LostAndFoundException,
            "AuthException should extend LostAndFoundException");
    }

    @Test
    @DisplayName("Test AuthException with empty message")
    void testAuthExceptionWithEmptyMessage() {
        // Arrange & Act
        AuthException exception = new AuthException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage(), "Exception should accept empty message");
    }

    @Test
    @DisplayName("Test AuthException with null message")
    void testAuthExceptionWithNullMessage() {
        // Arrange & Act
        AuthException exception = new AuthException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage(), "Exception message should be null");
    }

    @Test
    @DisplayName("Test AuthException can be thrown and caught")
    void testAuthExceptionCanBeThrown() {
        // Arrange
        String errorMessage = "Authentication failed";

        // Act & Assert
        assertThrows(AuthException.class, () -> {
            throw new AuthException(errorMessage);
        }, "AuthException should be throwable");
    }

    @Test
    @DisplayName("Test AuthException message is preserved when thrown")
    void testAuthExceptionMessagePreserved() {
        // Arrange
        String errorMessage = "User not found";

        // Act
        AuthException exception = assertThrows(AuthException.class, () -> {
            throw new AuthException(errorMessage);
        });

        // Assert
        assertEquals(errorMessage, exception.getMessage(),
            "Exception message should be preserved when thrown");
    }

    @Test
    @DisplayName("Test AuthException with detailed error message")
    void testAuthExceptionWithDetailedMessage() {
        // Arrange
        String detailedMessage = "Invalid username or password. Please try again.";

        // Act
        AuthException exception = new AuthException(detailedMessage);

        // Assert
        assertEquals(detailedMessage, exception.getMessage(),
            "Exception should preserve detailed error message");
    }

    @Test
    @DisplayName("Test multiple AuthException instances are independent")
    void testMultipleAuthExceptionInstances() {
        // Arrange
        String message1 = "First error";
        String message2 = "Second error";

        // Act
        AuthException exception1 = new AuthException(message1);
        AuthException exception2 = new AuthException(message2);

        // Assert
        assertNotSame(exception1, exception2, "Exceptions should be different instances");
        assertEquals(message1, exception1.getMessage());
        assertEquals(message2, exception2.getMessage());
    }
}

