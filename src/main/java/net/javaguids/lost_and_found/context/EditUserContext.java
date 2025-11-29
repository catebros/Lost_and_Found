package net.javaguids.lost_and_found.context;

import net.javaguids.lost_and_found.model.users.User;


// Context class for managing user data during edit operations.

public class EditUserContext {
    // Static field to hold the user being edited
    private static User userToEdit;

    // Sets the user to be edited in the context
    public static void setUser(User user) {
        userToEdit = user;
    }

    // Retrieves the user being edited from the context
    public static User getUser() {
        return userToEdit;
    }

    // Clears the user from the context 
    public static void clear() {
        userToEdit = null;
    }
}
