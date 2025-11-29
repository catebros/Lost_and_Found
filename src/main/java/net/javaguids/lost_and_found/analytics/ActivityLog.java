package net.javaguids.lost_and_found.analytics;

import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.model.users.User;
import java.time.LocalDateTime;
import java.util.UUID;

// Activity logging system for tracking user actions in the Lost and Found application.
public class ActivityLog {

    // Unique identifier for this log entry
    private String logId;

    // ID of the user who performed the action
    private String userId;

    // Type of action performed (e.g., "LOGIN", "POST_ITEM", "UPDATE_ITEM")
    private String action;

    // Detailed description of the action
    private String details;

    // Timestamp when the action occurred
    private LocalDateTime timestamp;

    // Creates a new activity log entry
    public ActivityLog(String logId, String userId, String action, String details) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Static method to log a user action
    public static void log(String userId, String action, String details) {
        String logId = UUID.randomUUID().toString();

        // Get username for better logging
        UserRepository userRepo = UserRepository.getInstance();
        User user = userRepo.getUserById(userId);
        String username = user != null ? user.getUsername() : "Unknown";

        // Format details to include username
        String formattedDetails = "[" + username + "] " + details;

        // Create and save the activity log
        ActivityLog log = new ActivityLog(logId, userId, action, formattedDetails);
        MessageRepository.getInstance().saveActivityLog(log);
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}