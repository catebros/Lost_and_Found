package net.javaguids.lost_and_found.model.items;

import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LostItemTest {
    private LostItem lostItem;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        lostItem = new LostItem(
            "LOST001",
            "Black Wallet",
            "Leather wallet with cards inside",
            "Wallet",
            "Library Building",
            "USER001",
            testDate,
            50.0
        );
    }

    @Test
    @DisplayName("Create lost item with valid data and verify all fields")
    void testLostItemCreation() {
        assertNotNull(lostItem);
        assertEquals("LOST001", lostItem.getItemId());
        assertEquals("Black Wallet", lostItem.getTitle());
        assertEquals("Leather wallet with cards inside", lostItem.getDescription());
        assertEquals("Wallet", lostItem.getCategory());
        assertEquals("Library Building", lostItem.getLocation());
        assertEquals("USER001", lostItem.getPostedByUserId());
        assertEquals(testDate, lostItem.getDateLost());
        assertEquals(50.0, lostItem.getReward());
        assertEquals(ItemType.LOST, lostItem.getType());
        assertEquals(ItemStatus.ACTIVE, lostItem.getStatus());
        assertNotNull(lostItem.getDatePosted());
    }

    @Test
    @DisplayName("Update all fields successfully")
    void testSetters() {
        lostItem.setTitle("Brown Wallet");
        lostItem.setDescription("New description");
        lostItem.setCategory("Personal Item");
        lostItem.setLocation("Cafeteria");
        lostItem.setStatus(ItemStatus.RESOLVED);
        lostItem.setDateLost(LocalDateTime.of(2024, 2, 1, 14, 0));
        lostItem.setReward(100.0);
        lostItem.setImagePath("/images/wallet.jpg");

        assertEquals("Brown Wallet", lostItem.getTitle());
        assertEquals("New description", lostItem.getDescription());
        assertEquals("Personal Item", lostItem.getCategory());
        assertEquals("Cafeteria", lostItem.getLocation());
        assertEquals(ItemStatus.RESOLVED, lostItem.getStatus());
        assertEquals(100.0, lostItem.getReward());
        assertEquals("/images/wallet.jpg", lostItem.getImagePath());
    }

    @Test
    @DisplayName("Match by search criteria")
    void testMatchesCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("wallet");
        criteria.setCategory("Wallet");
        criteria.setLocation("library");
        criteria.setType(ItemType.LOST);
        assertTrue(lostItem.matches(criteria));

        criteria.setType(ItemType.FOUND);
        assertFalse(lostItem.matches(criteria));
    }

    @Test
    @DisplayName("Get search keywords and toString")
    void testSearchKeywordsAndToString() {
        String keywords = lostItem.getSearchKeywords();
        assertTrue(keywords.contains("Black Wallet"));
        assertTrue(keywords.contains("Leather wallet with cards inside"));

        String result = lostItem.toString();
        assertTrue(result.contains("LOST"));
        assertTrue(result.contains("Black Wallet"));
    }
}
