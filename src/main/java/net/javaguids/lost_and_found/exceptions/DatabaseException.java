package net.javaguids.lost_and_found.exceptions;

// DatabaseException is a custom exception for database-related errors
public class DatabaseException extends LostAndFoundException {
    public DatabaseException(String message) {
        super(message);
    }
}