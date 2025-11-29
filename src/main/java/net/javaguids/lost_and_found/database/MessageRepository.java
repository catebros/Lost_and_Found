package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.analytics.ActivityLog;
// TODO: Implement Statistics class in analytics package
// import net.javaguids.lost_and_found.analytics.Statistics;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// TODO: Uncomment when Statistics class is implemented
// import java.util.HashMap;
// import java.util.Map;

/**
 * MessageRepository handles all database operations related to messages and activity logs.
 * This class implements the Singleton pattern to ensure only one instance exists.
 * 
 * Responsibilities:
 * - Saving and retrieving messages between users
 * - Managing activity logs for user actions
 * - Providing conversation history retrieval
 * - TODO: Generate statistics (requires Statistics class implementation)
 */
public class MessageRepository {
    // Singleton instance of MessageRepository
    private static MessageRepository instance;
    // Database connection obtained from DatabaseManager
    private final Connection connection;

    /**
     * Private constructor to prevent external instantiation (Singleton pattern).
     * Initializes the database connection through DatabaseManager.
     */
    private MessageRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Returns the singleton instance of MessageRepository.
     * Creates a new instance if one doesn't exist.
     * 
     * @return The singleton MessageRepository instance
     */
    public static MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
        }
        return instance;
    }

    /**
     * Saves a message to the database.
     * Uses INSERT OR REPLACE to handle both new messages and updates.
     * 
     * @param message The message object to save
     * @return true if the message was saved successfully, false otherwise
     */
    public boolean saveMessage(Message message) {
        String query = "INSERT OR REPLACE INTO messages (message_id, sender_id, receiver_id, content, timestamp) " +
                      "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, message.getMessageId());
            pstmt.setString(2, message.getSenderId());
            pstmt.setString(3, message.getReceiverId());
            pstmt.setString(4, message.getContent());
            pstmt.setString(5, message.getTimestamp().toString());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all messages for a specific user (both sent and received).
     * Messages are ordered by timestamp in descending order (newest first).
     * 
     * @param userId The ID of the user whose messages to retrieve
     * @return A list of messages associated with the user
     */
    public List<Message> getMessagesByUser(String userId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE (sender_id = ? OR receiver_id = ?) ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String messageId = rs.getString("message_id");
                String senderId = rs.getString("sender_id");
                String receiverId = rs.getString("receiver_id");
                String content = rs.getString("content");
                String timestampStr = rs.getString("timestamp");

                Message message = new Message(messageId, senderId, receiverId, content);
                // Set the original timestamp from database (preserves actual send time)
                if (timestampStr != null) {
                    message.setTimestamp(java.time.LocalDateTime.parse(timestampStr));
                }
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Retrieves all messages from the database.
     * Useful for administrative purposes or system-wide message retrieval.
     * Messages are ordered by timestamp in descending order (newest first).
     * 
     * @return A list of all messages in the database
     */
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String messageId = rs.getString("message_id");
                String senderId = rs.getString("sender_id");
                String receiverId = rs.getString("receiver_id");
                String content = rs.getString("content");
                String timestampStr = rs.getString("timestamp");

                Message message = new Message(messageId, senderId, receiverId, content);
                // Set the original timestamp from database (preserves actual send time)
                if (timestampStr != null) {
                    message.setTimestamp(java.time.LocalDateTime.parse(timestampStr));
                }
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Deletes a message from the database by its message ID.
     * 
     * @param messageId The unique identifier of the message to delete
     * @return true if the message was deleted successfully, false otherwise
     */
    public boolean deleteMessage(String messageId) {
        String query = "DELETE FROM messages WHERE message_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, messageId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a list of user IDs that the specified user has had conversations with.
     * Excludes system messages and returns distinct user IDs.
     * 
     * @param userId The ID of the user whose conversation partners to find
     * @return A list of user IDs that have exchanged messages with the specified user
     */
    public List<String> getUsersFromConversations(String userId) {
        List<String> users = new ArrayList<>();
        String query = "SELECT DISTINCT CASE " +
                "WHEN sender_id = ? THEN receiver_id " +
                "ELSE sender_id " +
                "END as other_user_id " +
                "FROM messages " +
                "WHERE sender_id = ? OR receiver_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            pstmt.setString(3, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String otherUserId = rs.getString("other_user_id");
                if (otherUserId != null && !otherUserId.equals("SYSTEM")) {
                    users.add(otherUserId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Saves an activity log entry to the database.
     * Activity logs track user actions for auditing and analytics purposes.
     * 
     * @param log The ActivityLog object to save
     * @return true if the log was saved successfully, false otherwise
     */
    public boolean saveActivityLog(ActivityLog log) {
        String query = "INSERT INTO activity_logs (log_id, user_id, action, details, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, log.getLogId());
            pstmt.setString(2, log.getUserId());
            pstmt.setString(3, log.getAction());
            pstmt.setString(4, log.getDetails());
            pstmt.setString(5, log.getTimestamp().toString());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves activity logs within a specified time range.
     * Useful for generating reports and analyzing user behavior over time.
     * 
     * @param from The start of the time range (inclusive)
     * @param to The end of the time range (inclusive)
     * @return A list of ActivityLog entries within the specified time range
     */
    public List<ActivityLog> getActivityLogs(LocalDateTime from, LocalDateTime to) {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT * FROM activity_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, from.toString());
            pstmt.setString(2, to.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String logId = rs.getString("log_id");
                String userId = rs.getString("user_id");
                String action = rs.getString("action");
                String details = rs.getString("details");

                ActivityLog log = new ActivityLog(logId, userId, action, details);
                log.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Generates statistics about the application.
     * TODO: Implement Statistics class in analytics package to enable this functionality
     * 
     * @return Statistics object containing application metrics
     */
    public Object generateStatistics() {
        // TODO: Implement Statistics class in analytics package
        // Statistics stats = new Statistics();
        //
        // try (Statement stmt = connection.createStatement()) {
        //     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
        //     if (rs.next()) {
        //         stats.setTotalUsers(rs.getInt("count"));
        //     }
        //
        //     rs = stmt.executeQuery("SELECT COUNT(*) as count FROM items");
        //     if (rs.next()) {
        //         stats.setTotalItems(rs.getInt("count"));
        //     }
        //
        //     rs = stmt.executeQuery("SELECT COUNT(*) as count FROM items WHERE type = 'LOST'");
        //     if (rs.next()) {
        //         stats.setTotalLostItems(rs.getInt("count"));
        //     }
        //
        //     rs = stmt.executeQuery("SELECT COUNT(*) as count FROM items WHERE type = 'FOUND'");
        //     if (rs.next()) {
        //         stats.setTotalFoundItems(rs.getInt("count"));
        //     }
        //
        //     rs = stmt.executeQuery("SELECT COUNT(*) as count FROM items WHERE status = 'RESOLVED'");
        //     if (rs.next()) {
        //         stats.setSuccessfulMatches(rs.getInt("count"));
        //     }
        //
        //     Map<String, Integer> itemsByCategory = new HashMap<>();
        //     rs = stmt.executeQuery("SELECT category, COUNT(*) as count FROM items GROUP BY category");
        //     while (rs.next()) {
        //         itemsByCategory.put(rs.getString("category"), rs.getInt("count"));
        //     }
        //     stats.setItemsByCategory(itemsByCategory);
        //
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
        //
        // return stats;
        
        return null;
    }
}
