package net.javaguids.lost_and_found.database;

import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.search.SearchCriteria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository {
    private static ItemRepository instance;
    private final Connection connection;

    private ItemRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public static ItemRepository getInstance() {
        if (instance == null) {
            instance = new ItemRepository();
        }
        return instance;
    }

    public Item getItemById(String itemId) {
        String query = "SELECT * FROM items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractItemFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveItem(Item item) {
        String query = "INSERT INTO items (item_id, title, description, category, location, date_posted, status, " +
                "posted_by_user_id, image_path, type, date_lost_found, reward) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getTitle());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getLocation());
            pstmt.setString(6, item.getDatePosted().toString());
            pstmt.setString(7, item.getStatus().toString());
            pstmt.setString(8, item.getPostedByUserId());
            pstmt.setString(9, item.getImagePath());
            pstmt.setString(10, item.getType().toString());

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                pstmt.setString(11, lostItem.getDateLost().toString());
                pstmt.setDouble(12, lostItem.getReward());
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                pstmt.setString(11, foundItem.getDateFound().toString());
                pstmt.setDouble(12, 0.0);
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateItem(Item item) {
        String query = "UPDATE items SET title = ?, description = ?, category = ?, location = ?, " +
                "status = ?, image_path = ?, date_lost_found = ?, reward = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getTitle());
            pstmt.setString(2, item.getDescription());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getLocation());
            pstmt.setString(5, item.getStatus().toString());
            pstmt.setString(6, item.getImagePath());

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                pstmt.setString(7, lostItem.getDateLost().toString());
                pstmt.setDouble(8, lostItem.getReward());
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                pstmt.setString(7, foundItem.getDateFound().toString());
                pstmt.setDouble(8, 0.0);
            }

            pstmt.setString(9, item.getItemId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(String itemId) {
        String query = "DELETE FROM items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> searchItems(SearchCriteria criteria) {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = extractItemFromResultSet(rs);
                // Use the Searchable interface's matches() method to filter items
                if (item.matches(criteria)) {
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<Item> getItemsByUser(String userId) {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items WHERE posted_by_user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(extractItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private Item extractItemFromResultSet(ResultSet rs) throws SQLException {
        String itemId = rs.getString("item_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String category = rs.getString("category");
        String location = rs.getString("location");
        String postedByUserId = rs.getString("posted_by_user_id");
        String type = rs.getString("type");
        String dateLostFoundStr = rs.getString("date_lost_found");

        Item item;
        if ("LOST".equals(type)) {
            LocalDateTime dateLost = LocalDateTime.parse(dateLostFoundStr);
            double reward = rs.getDouble("reward");
            item = new LostItem(itemId, title, description, category, location, postedByUserId, dateLost, reward);
        } else {
            LocalDateTime dateFound = LocalDateTime.parse(dateLostFoundStr);
            item = new FoundItem(itemId, title, description, category, location, postedByUserId, dateFound);
        }

        item.setStatus(ItemStatus.valueOf(rs.getString("status")));
        item.setImagePath(rs.getString("image_path"));
        return item;
    }
}