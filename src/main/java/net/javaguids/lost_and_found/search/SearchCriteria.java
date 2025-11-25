package net.javaguids.lost_and_found.search;

import net.javaguids.lost_and_found.model.enums.ItemType;
import java.time.LocalDateTime;

// Represents search criteria for filtering items in the lost and found system
public class SearchCriteria {
    private String keywords;
    private String category;
    private String location;
    private ItemType type;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    // Default constructor
    public SearchCriteria() {
    }

    // Validates that at least one search criterion is provided
    // Returns true if at least one of keywords, category, location, or type is set
    public boolean validate() {
        return keywords != null || category != null || location != null || type != null;
    }

    // Getters and setters for search criteria fields
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
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

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public LocalDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }
}