package net.javaguids.lost_and_found.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessagingException Tests")
class MessagingExceptionTest {

    @Test
    @DisplayName("Test MessagingException can be created with a message")
    void testMessagingExceptionCreation() {
        // Arrange & Act
        String errorMessage = "Failed to send message";
        MessagingException exception = new MessagingException(errorMessage);

        // Assert
        assertNotNull(exception, "MessagingException should be created");
        assertEquals(errorMessage, exception.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Test MessagingException extends LostAndFoundException")
    void testMessagingExceptionInheritance() {
        // Arrange & Act
        MessagingException exception = new MessagingException("Test message");

        // Assert
        assertTrue(exception instanceof LostAndFoundException,
            "MessagingException should extend LostAndFoundException");
    }

    @Test
    @DisplayName("Test MessagingException with empty message")
    void testMessagingExceptionWithEmptyMessage() {
        // Arrange & Act
        MessagingException exception = new MessagingException("");

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage(), "Exception should accept empty message");
    }

    @Test
    @DisplayName("Test MessagingException with null message")
    void testMessagingExceptionWithNullMessage() {
        // Arrange & Act
        MessagingException exception = new MessagingException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage(), "Exception message should be null");
    }

    @Test
    @DisplayName("Test MessagingException can be thrown and caught")
    void testMessagingExceptionCanBeThrown() {
        // Arrange
        String errorMessage = "Message delivery failed";

        // Act & Assert
        assertThrows(MessagingException.class, () -> {
            throw new MessagingException(errorMessage);
        }, "MessagingException should be throwable");
    }

    @Test
    @DisplayName("Test MessagingException message is preserved when thrown")
    void testMessagingExceptionMessagePreserved() {
        // Arrange
        String errorMessage = "Receiver not found";

        // Act
        MessagingException exception = assertThrows(MessagingException.class, () -> {
            throw new MessagingException(errorMessage);
        });

        // Assert
        assertEquals(errorMessage, exception.getMessage(),
            "Exception message should be preserved when thrown");
    }

    @Test
    @DisplayName("Test MessagingException with detailed error message")
    void testMessagingExceptionWithDetailedMessage() {
        // Arrange
        String detailedMessage = "Failed to send message: Connection timeout. Please try again.";

        // Act
        MessagingException exception = new MessagingException(detailedMessage);

        // Assert
        assertEquals(detailedMessage, exception.getMessage(),
            "Exception should preserve detailed error message");
    }

    @Test
    @DisplayName("Test multiple MessagingException instances are independent")
    void testMultipleMessagingExceptionInstances() {
        // Arrange
        String message1 = "First error";
        String message2 = "Second error";

        // Act
        MessagingException exception1 = new MessagingException(message1);
        MessagingException exception2 = new MessagingException(message2);

        // Assert
        assertNotSame(exception1, exception2, "Exceptions should be different instances");
        assertEquals(message1, exception1.getMessage());
        assertEquals(message2, exception2.getMessage());
    }
}

