package net.javaguids.lost_and_found.exceptions;

/**
 * Exception raised when attempting to persist or update an item
 * that violates validation rules.
 */
public class InvalidItemException extends net.javaguids.lost_and_found.exceptions.LostAndFoundException {

    /**
     * Creates a new exception with the provided message.
     *
     * @param message validation failure description
     */
    public InvalidItemException(String message) {
        super(message);
    }
}
