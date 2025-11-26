package net.javaguids.lost_and_found.model.items;

import net.javaguids.lost_and_found.model.enums.ItemType;
import java.time.LocalDateTime;

// Represents an item that has been lost by someone
public class LostItem extends Item {
    private LocalDateTime dateLost;
    private double reward;

    // Constructor to create a lost item with all required fields
    public LostItem(String itemId, String title, String description, String category,
                    String location, String postedByUserId, LocalDateTime dateLost, double reward) {
        super(itemId, title, description, category, location, postedByUserId);
        this.dateLost = dateLost;
        this.reward = reward;
    }

    // Setters and Getters
    @Override
    public ItemType getType() {
        return ItemType.LOST;
    }

    public LocalDateTime getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDateTime dateLost) {
        this.dateLost = dateLost;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }
}