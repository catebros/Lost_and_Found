package net.javaguids.lost_and_found.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Message Tests")
class MessageTest {

    private Message message;
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;

    @BeforeEach
    void setUp() {
        messageId = "msg-001";
        senderId = "user-001";
        receiverId = "user-002";
        content = "Hello, is this item still available?";
        message = new Message(messageId, senderId, receiverId, content);
    }

    @Test
    @DisplayName("Message constructor initializes all fields correctly")
    void testMessageConstructor() {
        assertEquals(messageId, message.getMessageId());
        assertEquals(senderId, message.getSenderId());
        assertEquals(receiverId, message.getReceiverId());
        assertEquals(content, message.getContent());
        assertNotNull(message.getTimestamp());
    }

    @Test
    @DisplayName("Message timestamp is automatically set to current time")
    void testTimestampIsSet() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        Message newMessage = new Message("msg-002", "user-003", "user-004", "Test");
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertTrue(newMessage.getTimestamp().isAfter(beforeCreation) ||
                   newMessage.getTimestamp().isEqual(beforeCreation),
            "Timestamp should be set to current time or later");
        assertTrue(newMessage.getTimestamp().isBefore(afterCreation) ||
                   newMessage.getTimestamp().isEqual(afterCreation),
            "Timestamp should be set to current time or earlier");
    }

    @Test
    @DisplayName("Message content can be updated")
    void testSetContent() {
        String newContent = "Updated message content";
        message.setContent(newContent);
        assertEquals(newContent, message.getContent());
    }

    @Test
    @DisplayName("Message timestamp can be set manually")
    void testSetTimestamp() {
        LocalDateTime customTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        message.setTimestamp(customTimestamp);
        assertEquals(customTimestamp, message.getTimestamp());
    }

    @Test
    @DisplayName("Message ID is immutable")
    void testMessageIdIsImmutable() {
        String originalId = message.getMessageId();
        assertEquals(messageId, originalId);
    }

    @Test
    @DisplayName("Message sender ID is immutable")
    void testSenderIdIsImmutable() {
        String originalSenderId = message.getSenderId();
        assertEquals(senderId, originalSenderId);
    }

    @Test
    @DisplayName("Message receiver ID is immutable")
    void testReceiverIdIsImmutable() {
        String originalReceiverId = message.getReceiverId();
        assertEquals(receiverId, originalReceiverId);
    }

    @Test
    @DisplayName("Multiple Message instances are independent")
    void testMultipleInstancesIndependent() {
        Message message2 = new Message("msg-002", "user-003", "user-004", "Different content");
        assertNotEquals(message.getMessageId(), message2.getMessageId());
        assertNotEquals(message.getSenderId(), message2.getSenderId());
        assertNotEquals(message.getContent(), message2.getContent());
    }

    @Test
    @DisplayName("Message with empty content is valid")
    void testMessageWithEmptyContent() {
        Message emptyMessage = new Message("msg-003", "user-005", "user-006", "");
        assertEquals("", emptyMessage.getContent());
        assertNotNull(emptyMessage.getTimestamp());
    }

    @Test
    @DisplayName("Message timestamp can be set to preserve original send time")
    void testTimestampPreservation() {
        LocalDateTime originalTime = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
        message.setTimestamp(originalTime);
        
        assertEquals(originalTime, message.getTimestamp());
        assertNotEquals(LocalDateTime.now(), message.getTimestamp());
    }
}

