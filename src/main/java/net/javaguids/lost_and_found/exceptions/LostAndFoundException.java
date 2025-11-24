package net.javaguids.lost_and_found.exceptions;

// LostAndFoundException is the base exception class for all custom exceptions
public class LostAndFoundException extends Exception {
    public LostAndFoundException(String message) {
        super(message);
    }
}