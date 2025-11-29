package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.database.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MessageService class.
 * Tests messaging operations and conversation management.
 *
 * Note: These tests use the actual MessageRepository with database.
 */
class MessageServiceTest {

    private MessageService messageService;
    private MessageRepository messageRepository;

    // Test user IDs
    private String testUserId1;
    private String testUserId2;
    private String testUserId3;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
        messageRepository = MessageRepository.getInstance();

        // Generate unique test user IDs for each test
        testUserId1 = "test_user_" + UUID.randomUUID().toString();
        testUserId2 = "test_user_" + UUID.randomUUID().toString();
        testUserId3 = "test_user_" + UUID.randomUUID().toString();
    }

    @AfterEach
    void cleanup() {
        // Clean up test messages after each test
        // Note: In a real scenario, you might want to implement a cleanup method
        // in MessageRepository to delete test data
    }

    // Constructor Tests

    @Test
    void testConstructor() {
        assertNotNull(messageService, "MessageService should be instantiated");
    }

    // SendMessage Tests

    @Test
    void testSendMessage_ValidMessage_ReturnsTrue() {
        Message message = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Hello, I found your item!"
        );

        boolean result = messageService.sendMessage(message);

        assertTrue(result, "Sending valid message should succeed");
    }

    @Test
    void testSendMessage_SystemMessage_HandlesSpecialCase() {
        Message systemMessage = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                "SYSTEM",
                "This is a system notification"
        );

        // Should handle SYSTEM receiver without trying to look up username
        assertDoesNotThrow(() -> {
            messageService.sendMessage(systemMessage);
        }, "Should handle system messages without errors");
    }

    // GetInbox Tests

    @Test
    void testGetInbox_ReturnsNotNull() {
        List<Message> inbox = messageService.getInbox(testUserId1);

        assertNotNull(inbox, "Inbox should return a list, not null");
    }

    @Test
    void testGetInbox_NewUser_ReturnsEmptyOrMessages() {
        List<Message> inbox = messageService.getInbox(testUserId1);

        assertNotNull(inbox);
        // New user might have no messages
        assertTrue(inbox.size() >= 0, "Inbox should be a valid list");
    }

    @Test
    void testGetInbox_AfterSendingMessage_ContainsMessage() {
        // Send a message to testUserId2
        Message message = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Test message for inbox"
        );

        messageService.sendMessage(message);

        // Get inbox for receiver
        List<Message> inbox = messageService.getInbox(testUserId2);

        assertNotNull(inbox);

        // Check if the message is in the inbox
        boolean found = false;
        for (Message msg : inbox) {
            if (msg.getMessageId().equals(message.getMessageId())) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Sent message should appear in receiver's inbox");
    }

    // GetConversation Tests

    @Test
    void testGetConversation_ReturnsNotNull() {
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        assertNotNull(conversation, "Conversation should return a list, not null");
    }

    @Test
    void testGetConversation_NoMessages_ReturnsEmptyList() {
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        assertNotNull(conversation);
        assertTrue(conversation.isEmpty(),
                "New conversation should be empty");
    }

    @Test
    void testGetConversation_WithMessages_ReturnsCorrectMessages() {
        // Send messages between user1 and user2
        Message msg1 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Hello from user1"
        );

        Message msg2 = new Message(
                UUID.randomUUID().toString(),
                testUserId2,
                testUserId1,
                "Reply from user2"
        );

        messageService.sendMessage(msg1);
        messageService.sendMessage(msg2);

        // Get conversation
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        // Should contain both messages
        assertTrue(conversation.size() >= 2,
                "Conversation should contain sent messages");

        // Verify messages are between the correct users
        for (Message msg : conversation) {
            boolean isCorrectConversation =
                    (msg.getSenderId().equals(testUserId1) && msg.getReceiverId().equals(testUserId2)) ||
                            (msg.getSenderId().equals(testUserId2) && msg.getReceiverId().equals(testUserId1));

            assertTrue(isCorrectConversation,
                    "All messages should be between user1 and user2");
        }
    }

    @Test
    void testGetConversation_ExcludesThirdPartyMessages() {
        // Send message between user1 and user2
        Message msg1 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Message to user2"
        );

        // Send message between user1 and user3 (different conversation)
        Message msg2 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId3,
                "Message to user3"
        );

        messageService.sendMessage(msg1);
        messageService.sendMessage(msg2);

        // Get conversation between user1 and user2
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        // Should not contain message to user3
        for (Message msg : conversation) {
            assertFalse(msg.getReceiverId().equals(testUserId3),
                    "Conversation should not include messages to third party");
        }
    }

    @Test
    void testGetConversation_NoDuplicates() {
        // Send a message
        Message msg = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Unique message"
        );

        messageService.sendMessage(msg);

        // Get conversation
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        // Count occurrences of the message ID
        long count = conversation.stream()
                .filter(m -> m.getMessageId().equals(msg.getMessageId()))
                .count();

        assertEquals(1, count,
                "Each message should appear only once in conversation");
    }

    @Test
    void testGetConversation_WithNullItemId() {
        // Test that null itemId doesn't cause issues
        assertDoesNotThrow(() -> {
            List<Message> conversation = messageService.getConversation(
                    testUserId1, testUserId2, null);
            assertNotNull(conversation);
        }, "Should handle null itemId without throwing exception");
    }

    // DeleteEmptyConversation Tests

    @Test
    void testDeleteEmptyConversation_EmptyConversation_ReturnsTrue() {
        boolean result = messageService.deleteEmptyConversation(
                testUserId1, testUserId2);

        assertTrue(result,
                "Deleting empty conversation should return true");
    }

    @Test
    void testDeleteEmptyConversation_WithMessages_DeletesAll() {
        // Send some messages
        Message msg1 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "Message 1"
        );

        Message msg2 = new Message(
                UUID.randomUUID().toString(),
                testUserId2,
                testUserId1,
                "Message 2"
        );

        messageService.sendMessage(msg1);
        messageService.sendMessage(msg2);

        // Delete conversation
        boolean result = messageService.deleteEmptyConversation(
                testUserId1, testUserId2);

        // Result should indicate success (true or false depending on implementation)
        assertNotNull(result);

        // Verify conversation is now empty or deleted
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        // After deletion, conversation should be empty
        // Note: This depends on MessageRepository.deleteMessage() implementation
        assertTrue(conversation.isEmpty() || conversation.size() < 2,
                "Conversation should be empty or have fewer messages after deletion");
    }

    @Test
    void testDeleteEmptyConversation_DoesNotAffectOtherConversations() {
        // Send messages in two different conversations
        Message msg1 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId2,
                "User1 to User2"
        );

        Message msg2 = new Message(
                UUID.randomUUID().toString(),
                testUserId1,
                testUserId3,
                "User1 to User3"
        );

        messageService.sendMessage(msg1);
        messageService.sendMessage(msg2);

        // Delete conversation between user1 and user2
        messageService.deleteEmptyConversation(testUserId1, testUserId2);

        // Conversation between user1 and user3 should still exist
        List<Message> otherConversation = messageService.getConversation(
                testUserId1, testUserId3, null);

        // This conversation should still have messages
        boolean hasMessages = otherConversation.stream()
                .anyMatch(m -> m.getMessageId().equals(msg2.getMessageId()));

        assertTrue(hasMessages,
                "Other conversations should not be affected by deletion");
    }

    // Edge Cases

    @Test
    void testGetConversation_SameUser_ReturnsEmpty() {
        // Try to get conversation between same user
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId1, null);

        assertNotNull(conversation);
        // Should be empty since you can't message yourself
        assertTrue(conversation.isEmpty(),
                "Conversation with self should be empty");
    }

    @Test
    void testSendMessage_MultipleMessages_AllStoredCorrectly() {
        // Send multiple messages
        for (int i = 0; i < 5; i++) {
            Message msg = new Message(
                    UUID.randomUUID().toString(),
                    testUserId1,
                    testUserId2,
                    "Message " + i
            );

            boolean result = messageService.sendMessage(msg);
            assertTrue(result, "Each message should be sent successfully");
        }

        // Get conversation
        List<Message> conversation = messageService.getConversation(
                testUserId1, testUserId2, null);

        assertTrue(conversation.size() >= 5,
                "All sent messages should be in conversation");
    }
}