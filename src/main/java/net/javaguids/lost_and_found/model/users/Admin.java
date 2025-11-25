package net.javaguids.lost_and_found.model.users;
import java.lang.String;
import net.javaguids.lost_and_found.model.enums.UserRole;

// Admin user type with full system access and management capabilities
public class Admin extends User {

    // Constructor
    public Admin(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, UserRole.ADMIN);
    }

    // Display the admin dashboard interface
    @Override
    public void displayDashboard() {
        System.out.println("=== Admin Dashboard ===");
        System.out.println("Welcome, Admin " + username + "!");
    }
}