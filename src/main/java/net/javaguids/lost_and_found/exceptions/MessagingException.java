package net.javaguids.lost_and_found.exceptions;

// Exception for messaging-related errors (sending, receiving, etc.)
public class MessagingException extends LostAndFoundException {
    public MessagingException(String message) {
        super(message);
    }
}
