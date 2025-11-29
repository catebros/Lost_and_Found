package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.analytics.ActivityLog;

import java.util.List;
import java.util.ArrayList;

// Service layer for messaging functionality.
public class MessageService {

    // Repository for user data access */
    private UserRepository userRepository;

    // Repository for message data access */
    private MessageRepository messageRepository;

    // Constructor initializes repositories for message operations.
    public MessageService() {
        this.userRepository = UserRepository.getInstance();
        this.messageRepository = MessageRepository.getInstance();
    }

    // Sends a message from one user to another and logs the action.
    public boolean sendMessage(Message message) {
        boolean success = messageRepository.saveMessage(message);

        if (success) {
            // Get receiver's username for better logging
            String receiverName = message.getReceiverId();

            // Don't look up username for system messages
            if (!message.getReceiverId().equals("SYSTEM")) {
                net.javaguids.lost_and_found.model.users.User receiver =
                        userRepository.getUserById(message.getReceiverId());
                if (receiver != null) {
                    receiverName = receiver.getUsername();
                }
            }

            // Log the message send action
            ActivityLog.log(message.getSenderId(), "SEND_MESSAGE",
                    "Sent message to user: " + receiverName);
        }

        return success;
    }

    // Retrieves all messages received by a specific user (inbox).
    public List<Message> getInbox(String userId) {
        return messageRepository.getMessagesByUser(userId);
    }

    // Retrieves the conversation between two users. Returns all messages exchanged between the two users, regardless of direction. Removes duplicates and can optionally filter by item ID.
    public List<Message> getConversation(String userId1, String userId2, String itemId) {
        // Get messages for both users
        List<Message> user1Messages = messageRepository.getMessagesByUser(userId1);
        List<Message> user2Messages = messageRepository.getMessagesByUser(userId2);

        // Combine both lists
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(user1Messages);
        allMessages.addAll(user2Messages);

        // Keep track of message IDs we've already added to avoid duplicates
        List<String> addedMessageIds = new ArrayList<>();
        List<Message> conversation = new ArrayList<>();

        // Filter messages to find ones between these two users
        for (Message message : allMessages) {
            // Check if this message is between userId1 and userId2
            boolean isBetweenUsers =
                    (message.getSenderId().equals(userId1) && message.getReceiverId().equals(userId2)) ||
                            (message.getSenderId().equals(userId2) && message.getReceiverId().equals(userId1));

            // Check if we've already added this message (avoid duplicates)
            boolean isNotDuplicate = !addedMessageIds.contains(message.getMessageId());

            // Add message if it's between the two users and not a duplicate
            if (isBetweenUsers && isNotDuplicate) {
                conversation.add(message);
                addedMessageIds.add(message.getMessageId());
            }
        }

        return conversation;
    }

    // Deletes an empty conversation between two users.
    public boolean deleteEmptyConversation(String userId1, String userId2) {
        // Get all messages between these two users
        List<Message> conversation = getConversation(userId1, userId2, null);

        // If conversation is empty, nothing to delete
        if (conversation.isEmpty()) {
            return true;
        }

        // Delete all messages in the conversation
        boolean allSuccess = true;
        for (Message message : conversation) {
            boolean success = messageRepository.deleteMessage(message.getMessageId());
            if (!success) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }
}