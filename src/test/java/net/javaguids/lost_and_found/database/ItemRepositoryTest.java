package net.javaguids.lost_and_found.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemRepository structure tests")
class ItemRepositoryTest {

    @Test
    @DisplayName("Singleton instance is reused")
    void testSingletonInstance() {
        ItemRepository repo1 = ItemRepository.getInstance();
        ItemRepository repo2 = ItemRepository.getInstance();
        assertNotNull(repo1);
        assertSame(repo1, repo2, "ItemRepository should implement Singleton pattern");
    }

    @Test
    @DisplayName("Lost item matches keyword criteria")
    void testLostItemMatchesKeyword() {
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            "item-1",
            "Blue Backpack",
            "Blue backpack with laptop",
            "Bags",
            "Campus Library",
            "user-1",
            LocalDateTime.now().minusDays(1),
            50.0
        );

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .keyword("backpack")
            .build();

        assertTrue(item.matches(criteria), "Keyword search should match item title");
    }

    @Test
    @DisplayName("Search criteria filters by location and status")
    void testSearchCriteriaFiltersLocationAndStatus() {
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            "item-2",
            "Phone",
            "Black phone case",
            "Electronics",
            "Cafeteria",
            "user-2",
            LocalDateTime.now().minusHours(6),
            0.0
        );
        item.setStatus(ItemRepository.ItemStatus.CLAIMED);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .location("Cafeteria")
            .status(ItemRepository.ItemStatus.CLAIMED)
            .build();

        assertTrue(item.matches(criteria), "Item should match combined location + status filters");
    }

    @Test
    @DisplayName("Search criteria respects type filtering")
    void testSearchCriteriaTypeFilter() {
        ItemRepository.LostItem lostItem = new ItemRepository.LostItem(
            "item-3",
            "Wallet",
            "Brown leather wallet",
            "Accessories",
            "Gym",
            "user-3",
            LocalDateTime.now().minusDays(2),
            20.0
        );

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .type(ItemRepository.ItemType.LOST)
            .build();

        assertTrue(lostItem.matches(criteria), "Lost item should match LOST type criteria");

        ItemRepository.SearchCriteria foundCriteria = new ItemRepository.SearchCriteria.Builder()
            .type(ItemRepository.ItemType.FOUND)
            .build();

        assertFalse(lostItem.matches(foundCriteria), "Lost item should not match FOUND type criteria");
    }
}

