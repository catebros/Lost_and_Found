package net.javaguids.lost_and_found.exceptions;

/**
 * MessagingException is thrown when errors occur during message operations.
 * This includes failures in sending, receiving, or managing messages.
 * 
 * Extends LostAndFoundException to maintain the exception hierarchy.
 */
public class MessagingException extends LostAndFoundException {
    /**
     * Creates a new MessagingException with the specified error message.
     * 
     * @param message The detailed error message explaining the messaging failure
     */
    public MessagingException(String message) {
        super(message);
    }
}
