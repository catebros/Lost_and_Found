package net.javaguids.lost_and_found.exceptions;

/**
 * Custom exception for authentication-related errors.
 * This exception is thrown when authentication operations fail,
 * such as invalid credentials, unauthorized access attempts, or login failures.
 * 
 * Extends LostAndFoundException to maintain the exception hierarchy.
 */
public class AuthException extends LostAndFoundException {
    /**
     * Creates a new AuthException with the specified error message.
     * 
     * @param message The detailed error message explaining the authentication failure
     */
    public AuthException(String message) {
        super(message);
    }
}
