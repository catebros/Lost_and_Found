package net.javaguids.lost_and_found.model.items;

import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FoundItemTest {
    private FoundItem foundItem;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        foundItem = new FoundItem(
            "FOUND001",
            "Blue Backpack",
            "Nike backpack with laptop inside",
            "Bag",
            "Classroom 101",
            "USER001",
            testDate
        );
    }

    @Test
    @DisplayName("Create found item with valid data and verify all fields")
    void testFoundItemCreation() {
        assertNotNull(foundItem);
        assertEquals("FOUND001", foundItem.getItemId());
        assertEquals("Blue Backpack", foundItem.getTitle());
        assertEquals("Nike backpack with laptop inside", foundItem.getDescription());
        assertEquals("Bag", foundItem.getCategory());
        assertEquals("Classroom 101", foundItem.getLocation());
        assertEquals("USER001", foundItem.getPostedByUserId());
        assertEquals(testDate, foundItem.getDateFound());
        assertEquals(ItemType.FOUND, foundItem.getType());
        assertEquals(ItemStatus.ACTIVE, foundItem.getStatus());
        assertNotNull(foundItem.getDatePosted());
    }

    @Test
    @DisplayName("Update all fields successfully")
    void testSetters() {
        foundItem.setTitle("Red Backpack");
        foundItem.setDescription("New description");
        foundItem.setCategory("Luggage");
        foundItem.setLocation("Library");
        foundItem.setStatus(ItemStatus.RESOLVED);
        foundItem.setDateFound(LocalDateTime.of(2024, 2, 1, 14, 0));
        foundItem.setImagePath("/images/backpack.jpg");

        assertEquals("Red Backpack", foundItem.getTitle());
        assertEquals("New description", foundItem.getDescription());
        assertEquals("Luggage", foundItem.getCategory());
        assertEquals("Library", foundItem.getLocation());
        assertEquals(ItemStatus.RESOLVED, foundItem.getStatus());
        assertEquals("/images/backpack.jpg", foundItem.getImagePath());
    }

    @Test
    @DisplayName("Match by search criteria")
    void testMatchesCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("backpack");
        criteria.setCategory("Bag");
        criteria.setLocation("classroom");
        criteria.setType(ItemType.FOUND);
        assertTrue(foundItem.matches(criteria));

        criteria.setType(ItemType.LOST);
        assertFalse(foundItem.matches(criteria));
    }

    @Test
    @DisplayName("Get search keywords and toString")
    void testSearchKeywordsAndToString() {
        String keywords = foundItem.getSearchKeywords();
        assertTrue(keywords.contains("Blue Backpack"));
        assertTrue(keywords.contains("Nike backpack with laptop inside"));

        String result = foundItem.toString();
        assertTrue(result.contains("FOUND"));
        assertTrue(result.contains("Blue Backpack"));
    }
}
