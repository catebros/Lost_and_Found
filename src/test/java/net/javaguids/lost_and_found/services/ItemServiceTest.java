package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.search.SearchCriteria;
import net.javaguids.lost_and_found.model.enums.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

// Test suite for ItemService class. Tests business logic for item operations
class ItemServiceTest {

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();
    }

    @Test
    void testConstructor() {
        assertNotNull(itemService, "ItemService should be instantiated");
    }


    @Test
    void testSearchItems_ReturnsEmptyList() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("wallet");

        List<Item> results = itemService.searchItems(criteria);

        assertNotNull(results, "Search should return a list, not null");
        assertTrue(results.isEmpty(), "Search should return empty list when no items exist");
    }

    @Test
    void testSearchItemsExcludingUser_ReturnsEmptyList() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("phone");

        List<Item> results = itemService.searchItemsExcludingUser(criteria, "user-123");

        assertNotNull(results, "Search should return a list, not null");
        assertTrue(results.isEmpty(), "Search should return empty list when no items exist");
    }

    @Test
    void testGetItemById_ReturnsNull() {
        Item result = itemService.getItemById("item-123");

        assertNull(result, "Should return null when ItemRepository is not implemented");
    }

    @Test
    void testGetItemsByUser_ReturnsEmptyList() {
        List<Item> results = itemService.getItemsByUser("user-456");

        assertNotNull(results, "Should return a list, not null");
        assertTrue(results.isEmpty(), "Should return empty list when no items exist");
    }
}