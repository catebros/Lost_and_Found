package net.javaguids.lost_and_found.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
// Handles saving and loading items from the database
// Uses singleton pattern to keep one instance
public class ItemRepository {
    private static ItemRepository instance;
    private final Connection connection;

    private ItemRepository() {
        this.connection = net.javaguids.lost_and_found.database.DatabaseManager.getInstance().getConnection();
    }

    public static ItemRepository getInstance() {
        if (instance == null) {
            instance = new ItemRepository();
        }
        return instance;
    }

    // Gets an item by ID, returns null if not found
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

    // Saves a new item to the database
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
            } else {
                pstmt.setString(11, item.getDatePosted().toString());
                pstmt.setDouble(12, 0.0);
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates an existing item
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
            } else {
                pstmt.setString(7, item.getDatePosted().toString());
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

    // Deletes an item by ID
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

    // Searches items using the given criteria (keyword, location, status, type)
    public List<Item> searchItems(SearchCriteria criteria) {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = extractItemFromResultSet(rs);
                if (item.matches(criteria)) {
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Gets all items posted by a specific user
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

    // Converts a database row into a LostItem or FoundItem
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
        if (Objects.equals(type, ItemType.LOST.name())) {
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
    // Possible statuses for items
    public enum ItemStatus {
        ACTIVE,
        CLAIMED,
        RESOLVED
    }

    // Item type - either lost or found
    public enum ItemType {
        LOST,
        FOUND
    }

    // Search criteria for filtering items (uses builder pattern)
    public static class SearchCriteria {
        private final String keyword;
        private final String location;
        private final ItemStatus status;
        private final ItemType type;

        private SearchCriteria(Builder builder) {
            this.keyword = builder.keyword;
            this.location = builder.location;
            this.status = builder.status;
            this.type = builder.type;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getLocation() {
            return location;
        }

        public ItemStatus getStatus() {
            return status;
        }

        public ItemType getType() {
            return type;
        }

        // Builder for creating SearchCriteria
        public static class Builder {
            private String keyword;
            private String location;
            private ItemStatus status;
            private ItemType type;

            public Builder keyword(String keyword) {
                this.keyword = keyword;
                return this;
            }

            public Builder location(String location) {
                this.location = location;
                return this;
            }

            public Builder status(ItemStatus status) {
                this.status = status;
                return this;
            }

            public Builder type(ItemType type) {
                this.type = type;
                return this;
            }

            public SearchCriteria build() {
                return new SearchCriteria(this);
            }
        }
    }

    // Interface for things that can be matched against search criteria
    public interface Searchable {
        boolean matches(SearchCriteria criteria);
    }

    // Base class for LostItem and FoundItem
    public static abstract class Item implements Searchable {
        private final String itemId;
        private final String title;
        private final String description;
        private final String category;
        private final String location;
        private final String postedByUserId;
        private final LocalDateTime datePosted;
        private final ItemType type;
        private ItemStatus status;
        private String imagePath;

        protected Item(String itemId,
                       String title,
                       String description,
                       String category,
                       String location,
                       String postedByUserId,
                       ItemType type) {
            this.itemId = itemId != null ? itemId : UUID.randomUUID().toString();
            this.title = title;
            this.description = description;
            this.category = category;
            this.location = location;
            this.postedByUserId = postedByUserId;
            this.type = type;
            this.datePosted = LocalDateTime.now();
            this.status = ItemStatus.ACTIVE;
        }

        public String getItemId() {
            return itemId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public String getLocation() {
            return location;
        }

        public String getPostedByUserId() {
            return postedByUserId;
        }

        public LocalDateTime getDatePosted() {
            return datePosted;
        }

        public ItemStatus getStatus() {
            return status;
        }

        public void setStatus(ItemStatus status) {
            this.status = status;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public ItemType getType() {
            return type;
        }

        @Override
        public boolean matches(SearchCriteria criteria) {
            if (criteria == null) {
                return true;
            }
            boolean matchesKeyword = criteria.getKeyword() == null ||
                                     title.toLowerCase().contains(criteria.getKeyword().toLowerCase()) ||
                                     description.toLowerCase().contains(criteria.getKeyword().toLowerCase());
            boolean matchesLocation = criteria.getLocation() == null ||
                                      Objects.equals(location, criteria.getLocation());
            boolean matchesStatus = criteria.getStatus() == null ||
                                    status == criteria.getStatus();
            boolean matchesType = criteria.getType() == null || type == criteria.getType();
            return matchesKeyword && matchesLocation && matchesStatus && matchesType;
        }
    }

    // Item that someone lost
    public static final class LostItem extends Item {
        private final LocalDateTime dateLost;
        private final double reward;

        public LostItem(String itemId,
                        String title,
                        String description,
                        String category,
                        String location,
                        String postedByUserId,
                        LocalDateTime dateLost,
                        double reward) {
            super(itemId, title, description, category, location, postedByUserId, ItemType.LOST);
            this.dateLost = dateLost;
            this.reward = reward;
        }

        public LocalDateTime getDateLost() {
            return dateLost;
        }

        public double getReward() {
            return reward;
        }
    }

    // Item that someone found
    public static final class FoundItem extends Item {
        private final LocalDateTime dateFound;

        public FoundItem(String itemId,
                         String title,
                         String description,
                         String category,
                         String location,
                         String postedByUserId,
                         LocalDateTime dateFound) {
            super(itemId, title, description, category, location, postedByUserId, ItemType.FOUND);
            this.dateFound = dateFound;
        }

        public LocalDateTime getDateFound() {
            return dateFound;
        }
    }
}
