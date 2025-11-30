package net.javaguids.lost_and_found.analytics;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private int totalUsers;
    private int totalItems;
    private int totalLostItems;
    private int totalFoundItems;
    private int successfulMatches;
    private Map<String, Integer> itemsByCategory;

    public Statistics() {
        this.itemsByCategory = new HashMap<>();
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Lost and Found Statistics\n");
        report.append("Total Users: ").append(totalUsers).append("\n");
        report.append("Total Items: ").append(totalItems).append("\n");
        report.append("Lost Items: ").append(totalLostItems).append("\n");
        report.append("Found Items: ").append(totalFoundItems).append("\n");
        report.append("Successful Matches: ").append(successfulMatches).append("\n");
        report.append("\nItems by Category:\n");
        for (String category : itemsByCategory.keySet()) {
            int count = itemsByCategory.get(category);
            report.append("  ");
            report.append(category);
            report.append(": ");
            report.append(count);
            report.append("\n");
        }
        return report.toString();
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalLostItems() {
        return totalLostItems;
    }

    public void setTotalLostItems(int totalLostItems) {
        this.totalLostItems = totalLostItems;
    }

    public int getTotalFoundItems() {
        return totalFoundItems;
    }

    public void setTotalFoundItems(int totalFoundItems) {
        this.totalFoundItems = totalFoundItems;
    }

    public int getSuccessfulMatches() {
        return successfulMatches;
    }

    public void setSuccessfulMatches(int successfulMatches) {
        this.successfulMatches = successfulMatches;
    }

    public Map<String, Integer> getItemsByCategory() {
        return itemsByCategory;
    }

    public void setItemsByCategory(Map<String, Integer> itemsByCategory) {
        this.itemsByCategory = itemsByCategory;
    }
}