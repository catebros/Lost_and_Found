package net.javaguids.lost_and_found.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemNotFoundException tests")
class ItemNotFoundExceptionTest {

    @Test
    @DisplayName("Exception exposes provided message")
    void testMessagePropagation() {
        ItemNotFoundException exception = new ItemNotFoundException("Item missing");
        assertEquals("Item missing", exception.getMessage());
    }

    @Test
    @DisplayName("Exception subclasses LostAndFoundException")
    void testInheritance() {
        ItemNotFoundException exception = new ItemNotFoundException("test");
        assertTrue(exception instanceof LostAndFoundException,
            "ItemNotFoundException should extend LostAndFoundException");
    }

    @Test
    @DisplayName("Exception can be thrown and caught")
    void testThrowingException() {
        assertThrows(ItemNotFoundException.class, () -> {
            throw new ItemNotFoundException("not found");
        });
    }
}
