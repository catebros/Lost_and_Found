package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import net.javaguids.lost_and_found.analytics.Statistics;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Handles database operations for messages and activity logs
// Uses singleton pattern to keep one instance
public class MessageRepository {
    private static MessageRepository instance;
    private final Connection connection;

    private MessageRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public static MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
        }
        return instance;
    }

    // Saves a message to the database (INSERT OR REPLACE handles both new and updates)
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

    // Gets all messages for a user (sent and received), newest first
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
                // Restore original timestamp from DB
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

    // Gets all messages in the database (for admin use), newest first
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
                // Restore original timestamp from DB
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

    // Deletes a message by ID
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

    // Gets list of user IDs that this user has messaged with (excludes SYSTEM)
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

    // Saves an activity log to the database
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

    // Gets activity logs within a time range
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

    // TODO: Need to implement Statistics class first
    public Object generateStatistics() {
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
