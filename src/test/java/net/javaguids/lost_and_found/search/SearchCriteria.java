package net.javaguids.lost_and_found.search;

import net.javaguids.lost_and_found.model.enums.ItemType;
import java.time.LocalDateTime;

// Search criteria builder for filtering items in the Lost and Found system
public class SearchCriteria {

    // Keywords to search in item title and description */
    private String keywords;

    // Category filter (e.g., "Electronics", "Clothing", "Personal Items") */
    private String category;

    // Location where item was lost or found */
    private String location;

    // Type of item (LOST or FOUND) */
    private ItemType type;

    // Start date for date range filter */
    private LocalDateTime dateFrom;

    /** End date for date range filter */
    private LocalDateTime dateTo;

    // Default constructor for SearchCriteria. All fields are optional and can be set individually.
    public SearchCriteria() {
    }

    // Validates that at least one search criterion is provided.
    public boolean validate() {
        return keywords != null || category != null || location != null || type != null;
    }

    // Getters and Setters
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