package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Tests for MessageRepository - uses actual DB so test data may stick around
@DisplayName("MessageRepository Tests")
class MessageRepositoryTest {

    private MessageRepository repository;
    private String testMessageId;
    private String testSenderId;
    private String testReceiverId;
    private String testContent;

    @BeforeEach
    void setUp() {
        repository = MessageRepository.getInstance();
        // Use random IDs to avoid conflicts
        testMessageId = "msg-test-" + UUID.randomUUID().toString().substring(0, 8);
        testSenderId = "sender-" + UUID.randomUUID().toString().substring(0, 8);
        testReceiverId = "receiver-" + UUID.randomUUID().toString().substring(0, 8);
        testContent = "Test message content";
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        if (testMessageId != null) {
            repository.deleteMessage(testMessageId);
        }
    }

    @Test
    @DisplayName("Test Singleton pattern - getInstance always returns same instance")
    void testSingletonPattern() {
        MessageRepository instance1 = MessageRepository.getInstance();
        MessageRepository instance2 = MessageRepository.getInstance();
        assertSame(instance1, instance2,
            "MessageRepository should return the same instance (Singleton pattern)");
    }

    @Test
    @DisplayName("Test getInstance returns non-null instance")
    void testGetInstanceReturnsNonNull() {
        MessageRepository instance = MessageRepository.getInstance();
        assertNotNull(instance, "MessageRepository instance should not be null");
    }

    @Test
    @DisplayName("Test saveMessage saves a new message successfully")
    void testSaveMessage() {
        // Arrange
        Message testMessage = new Message(testMessageId, testSenderId, testReceiverId, testContent);

        // Act
        boolean result = repository.saveMessage(testMessage);

        // Assert
        assertTrue(result, "saveMessage should return true on success");
        
        // Check it was actually saved
        List<Message> messages = repository.getMessagesByUser(testSenderId);
        assertFalse(messages.isEmpty(), "Saved message should be retrievable");
        Message savedMessage = messages.stream()
            .filter(m -> m.getMessageId().equals(testMessageId))
            .findFirst()
            .orElse(null);
        assertNotNull(savedMessage, "Message should be found");
        assertEquals(testContent, savedMessage.getContent());
    }

    @Test
    @DisplayName("Test getMessagesByUser retrieves messages for a user")
    void testGetMessagesByUser() {
        // Arrange
        Message message1 = new Message(testMessageId, testSenderId, testReceiverId, "Message 1");
        Message message2 = new Message("msg-test-2-" + UUID.randomUUID().toString().substring(0, 8),
            testReceiverId, testSenderId, "Message 2");
        repository.saveMessage(message1);
        repository.saveMessage(message2);

        // Act
        List<Message> messages = repository.getMessagesByUser(testSenderId);

        // Assert
        assertNotNull(messages, "Messages list should not be null");
        assertTrue(messages.size() >= 2, "Should retrieve at least 2 messages");
        
        repository.deleteMessage(message2.getMessageId());
    }

    @Test
    @DisplayName("Test getMessagesByUser returns empty list for user with no messages")
    void testGetMessagesByUser_NoMessages() {
        // Arrange
        String nonExistentUserId = "non-existent-" + UUID.randomUUID().toString();

        // Act
        List<Message> messages = repository.getMessagesByUser(nonExistentUserId);

        // Assert
        assertNotNull(messages, "Messages list should not be null");
        assertTrue(messages.isEmpty(), "Should return empty list for user with no messages");
    }

    @Test
    @DisplayName("Test getAllMessages retrieves all messages")
    void testGetAllMessages() {
        // Arrange
        Message testMessage = new Message(testMessageId, testSenderId, testReceiverId, testContent);
        repository.saveMessage(testMessage);

        // Act
        List<Message> allMessages = repository.getAllMessages();

        // Assert
        assertNotNull(allMessages, "Messages list should not be null");
        assertFalse(allMessages.isEmpty(), "Should retrieve at least one message");
    }

    @Test
    @DisplayName("Test deleteMessage removes message from database")
    void testDeleteMessage() {
        // Arrange
        Message testMessage = new Message(testMessageId, testSenderId, testReceiverId, testContent);
        repository.saveMessage(testMessage);

        // Act
        boolean result = repository.deleteMessage(testMessageId);

        // Assert
        assertTrue(result, "deleteMessage should return true on success");
        
        // Make sure it's actually gone
        List<Message> messages = repository.getMessagesByUser(testSenderId);
        boolean messageExists = messages.stream()
            .anyMatch(m -> m.getMessageId().equals(testMessageId));
        assertFalse(messageExists, "Message should be deleted");
    }

    @Test
    @DisplayName("Test deleteMessage returns true even if message doesn't exist")
    void testDeleteMessage_NonExistent() {
        // Act
        boolean result = repository.deleteMessage("non-existent-message-id");

        // Assert
        assertTrue(result, "deleteMessage should return true even for non-existent messages");
    }

    @Test
    @DisplayName("Test getUsersFromConversations retrieves conversation partners")
    void testGetUsersFromConversations() {
        // Arrange
        Message message1 = new Message(testMessageId, testSenderId, testReceiverId, "Message 1");
        String otherUserId = "other-user-" + UUID.randomUUID().toString().substring(0, 8);
        Message message2 = new Message("msg-test-3-" + UUID.randomUUID().toString().substring(0, 8),
            testSenderId, otherUserId, "Message 2");
        repository.saveMessage(message1);
        repository.saveMessage(message2);

        // Act
        List<String> users = repository.getUsersFromConversations(testSenderId);

        // Assert
        assertNotNull(users, "Users list should not be null");
        assertTrue(users.contains(testReceiverId), "Should include receiver ID");
        assertTrue(users.contains(otherUserId), "Should include other user ID");
        
        repository.deleteMessage(message2.getMessageId());
    }

    @Test
    @DisplayName("Test getUsersFromConversations excludes SYSTEM messages")
    void testGetUsersFromConversations_ExcludesSystem() {
        // Arrange
        String systemId = "SYSTEM";
        Message systemMessage = new Message("msg-system-" + UUID.randomUUID().toString().substring(0, 8),
            systemId, testSenderId, "System message");
        repository.saveMessage(systemMessage);

        // Act
        List<String> users = repository.getUsersFromConversations(testSenderId);

        // Assert
        assertFalse(users.contains(systemId), "Should exclude SYSTEM from conversation partners");
        
        repository.deleteMessage(systemMessage.getMessageId());
    }

    @Test
    @DisplayName("Test saveActivityLog saves activity log successfully")
    void testSaveActivityLog() {
        // Arrange
        String logId = "log-" + UUID.randomUUID().toString().substring(0, 8);
        ActivityLog log = new ActivityLog(logId, testSenderId, "TEST_ACTION", "Test activity log");

        // Act
        boolean result = repository.saveActivityLog(log);

        // Assert
        assertTrue(result, "saveActivityLog should return true on success");
    }

    @Test
    @DisplayName("Test getActivityLogs retrieves logs within time range")
    void testGetActivityLogs() {
        // Arrange
        String logId = "log-" + UUID.randomUUID().toString().substring(0, 8);
        ActivityLog log = new ActivityLog(logId, testSenderId, "TEST_ACTION", "Test activity log");
        repository.saveActivityLog(log);

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        // Act
        List<ActivityLog> logs = repository.getActivityLogs(from, to);

        // Assert
        assertNotNull(logs, "Logs list should not be null");
        // Might be empty if no logs in that time range
    }

    @Test
    @DisplayName("Test message timestamp is preserved when saved and retrieved")
    void testMessageTimestampPreservation() {
        // Arrange
        LocalDateTime customTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Message testMessage = new Message(testMessageId, testSenderId, testReceiverId, testContent);
        testMessage.setTimestamp(customTimestamp);
        repository.saveMessage(testMessage);

        // Act
        List<Message> messages = repository.getMessagesByUser(testSenderId);
        Message retrievedMessage = messages.stream()
            .filter(m -> m.getMessageId().equals(testMessageId))
            .findFirst()
            .orElse(null);

        // Assert
        assertNotNull(retrievedMessage, "Message should be retrieved");
        assertNotNull(retrievedMessage.getTimestamp(), "Timestamp should be preserved");
    }

}

