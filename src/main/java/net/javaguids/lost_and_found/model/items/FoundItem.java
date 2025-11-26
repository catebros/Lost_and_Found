package net.javaguids.lost_and_found.model.items;

import net.javaguids.lost_and_found.model.enums.ItemType;
import java.time.LocalDateTime;

// Represents an item that has been found by someone
public class FoundItem extends Item {
    // Date and time when the item was found
    private LocalDateTime dateFound;

    // Constructor to create a found item with all required fields
    public FoundItem(String itemId, String title, String description, String category,
                     String location, String postedByUserId, LocalDateTime dateFound) {
        super(itemId, title, description, category, location, postedByUserId);
        this.dateFound = dateFound;
    }

    // Setters and Getters
    @Override
    public ItemType getType() {
        return ItemType.FOUND;
    }

    public LocalDateTime getDateFound() {
        return dateFound;
    }

    public void setDateFound(LocalDateTime dateFound) {
        this.dateFound = dateFound;
    }
}