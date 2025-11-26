package net.javaguids.lost_and_found.exceptions;

/**
 * Exception thrown when an item lookup fails to locate the requested entity.
 */
public class ItemNotFoundException extends net.javaguids.lost_and_found.exceptions.LostAndFoundException {

    /**
     * Creates a new exception with the provided message.
     *
     * @param message human readable explanation of the failure
     */
    public ItemNotFoundException(String message) {
        super(message);
    }
}
