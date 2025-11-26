package net.javaguids.lost_and_found.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidItemException tests")
class InvalidItemExceptionTest {

    @Test
    @DisplayName("Exception stores provided message")
    void testMessagePropagation() {
        InvalidItemException exception = new InvalidItemException("Invalid payload");
        assertEquals("Invalid payload", exception.getMessage());
    }

    @Test
    @DisplayName("Exception inherits LostAndFoundException")
    void testInheritance() {
        InvalidItemException exception = new InvalidItemException("test");
        assertTrue(exception instanceof LostAndFoundException,
            "InvalidItemException should extend LostAndFoundException");
    }

    @Test
    @DisplayName("Exception can be thrown and captured")
    void testThrowingException() {
        assertThrows(InvalidItemException.class, () -> {
            throw new InvalidItemException("invalid state");
        });
    }
}

