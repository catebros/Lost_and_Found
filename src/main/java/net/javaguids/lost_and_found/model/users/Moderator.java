package net.javaguids.lost_and_found.model.users;
import java.lang.String;
import net.javaguids.lost_and_found.model.enums.UserRole;

// Moderator user type with elevated permissions to verify items and view chats
public class Moderator extends User {

    // Constructor
    public Moderator(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, UserRole.MODERATOR);
    }

    // Display the moderator dashboard interface
    @Override
    public void displayDashboard() {
        System.out.println("=== Moderator Dashboard ===");
        System.out.println("Welcome, Moderator " + username + "!");
    }
}