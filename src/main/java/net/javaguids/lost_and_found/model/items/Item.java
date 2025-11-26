package net.javaguids.lost_and_found.model.items;

import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.model.interfaces.Searchable;
import net.javaguids.lost_and_found.search.SearchCriteria;
import java.time.LocalDateTime;

// Abstract base class for all items in the lost and found system
public abstract class Item implements Searchable {
    protected String itemId;
    protected String title;
    protected String description;
    protected String category;
    protected String location;
    protected LocalDateTime datePosted;
    protected ItemStatus status;
    protected String postedByUserId;
    protected String imagePath;

    // Constructor to initialize an item with required fields
    // Automatically sets datePosted to current time and status to ACTIVE
    public Item(String itemId, String title, String description, String category,
                String location, String postedByUserId) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.postedByUserId = postedByUserId;
        this.datePosted = LocalDateTime.now();
        this.status = ItemStatus.ACTIVE;
    }

    // Returns the type of item (LOST or FOUND)
    // Must be implemented by subclasses
    public abstract ItemType getType();

    // Getters and setters for item properties
    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getPostedByUserId() {
        return postedByUserId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Checks if this item matches the given search criteria
    // Returns true if the item matches all non-null criteria fields
    // If criteria is null, returns true (matches everything)
    @Override
    public boolean matches(SearchCriteria criteria) {
        // Null criteria means match all items
        if (criteria == null) {
            return true;
        }

        // Check if keywords appear in title or description (case-insensitive)
        if (criteria.getKeywords() != null && !criteria.getKeywords().isEmpty()) {
            String keywords = criteria.getKeywords().toLowerCase();
            boolean keywordMatch = (title != null && title.toLowerCase().contains(keywords)) ||
                                   (description != null && description.toLowerCase().contains(keywords));
            if (!keywordMatch) {
                return false;
            }
        }

        // Check if category matches exactly
        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            if (category == null || !category.equals(criteria.getCategory())) {
                return false;
            }
        }

        // Check if location contains the search location (case-insensitive)
        if (criteria.getLocation() != null && !criteria.getLocation().isEmpty()) {
            if (location == null || !location.toLowerCase().contains(criteria.getLocation().toLowerCase())) {
                return false;
            }
        }

        // Check if item type matches
        if (criteria.getType() != null) {
            if (getType() != criteria.getType()) {
                return false;
            }
        }

        // All criteria matched
        return true;
    }

    // Returns a concatenated string of all searchable fields
    // Used for keyword searching across all item properties
    @Override
    public String getSearchKeywords() {
        StringBuilder keywords = new StringBuilder();
        if (title != null) keywords.append(title).append(" ");
        if (description != null) keywords.append(description).append(" ");
        if (category != null) keywords.append(category).append(" ");
        if (location != null) keywords.append(location).append(" ");
        return keywords.toString().trim();
    }

    // Returns a formatted string representation of the item
    // Format: [TYPE] title - category (location)
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)",
            getType(),
            title != null ? title : "Untitled",
            category != null ? category : "No category",
            location != null ? location : "No location");
    }
}