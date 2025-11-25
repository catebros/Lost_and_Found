package net.javaguids.lost_and_found.model.users;
import java.lang.String;
import net.javaguids.lost_and_found.model.enums.UserRole;

// Regular user type with basic permissions for posting and viewing lost/found items
public class RegularUser extends User {

    // Constructor initializes a regular user with standard user role
    public RegularUser(String userId, String username, String email, String passwordHash) {
        super(userId, username, email, passwordHash, UserRole.USER);
    }

    // Display the user dashboard interface
    @Override
    public void displayDashboard() {
        System.out.println("=== User Dashboard ===");
        System.out.println("Welcome, " + username + "!");
    }
}