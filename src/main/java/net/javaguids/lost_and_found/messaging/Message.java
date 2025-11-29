package net.javaguids.lost_and_found.messaging;

import java.time.LocalDateTime;

/**
 * Message represents a communication between two users in the Lost and Found system.
 * Messages are used for users to communicate about lost or found items.
 * 
 * Each message contains:
 * - A unique message ID
 * - Sender and receiver user IDs
 * - Message content
 * - Timestamp of when the message was created
 */
public class Message {
    // Unique identifier for this message
    private String messageId;
    // ID of the user who sent the message
    private String senderId;
    // ID of the user who receives the message
    private String receiverId;
    // The actual message content/text
    private String content;
    // Timestamp when the message was created
    private LocalDateTime timestamp;

    /**
     * Creates a new Message with the specified details.
     * The timestamp is automatically set to the current time.
     * 
     * @param messageId Unique identifier for the message
     * @param senderId ID of the user sending the message
     * @param receiverId ID of the user receiving the message
     * @param content The message text/content
     */
    public Message(String messageId, String senderId, String receiverId, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the unique message identifier.
     * 
     * @return The message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the ID of the user who sent the message.
     * 
     * @return The sender's user ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Gets the ID of the user who receives the message.
     * 
     * @return The receiver's user ID
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Gets the message content/text.
     * 
     * @return The message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content/text.
     * Allows updating the message content if needed.
     * 
     * @param content The new message content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp when the message was created.
     * 
     * @return The message timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for the message.
     * Useful when loading messages from the database to preserve original send time.
     * 
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
