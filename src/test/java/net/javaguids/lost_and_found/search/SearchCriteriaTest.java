package net.javaguids.lost_and_found.search;

import net.javaguids.lost_and_found.model.enums.ItemType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

// Test suite for SearchCriteria class. Tests validation and getter/setter functionality
class SearchCriteriaTest {

    @Test
    void testDefaultConstructor() {
        SearchCriteria criteria = new SearchCriteria();

        assertNull(criteria.getKeywords());
        assertNull(criteria.getCategory());
        assertNull(criteria.getLocation());
        assertNull(criteria.getType());
        assertNull(criteria.getDateFrom());
        assertNull(criteria.getDateTo());
    }

    @Test
    void testValidate_EmptyCriteria() {
        SearchCriteria criteria = new SearchCriteria();

        assertFalse(criteria.validate(),
                "Empty criteria should be invalid");
    }

    @Test
    void testValidate_WithKeywords() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("wallet");

        assertTrue(criteria.validate(),
                "Criteria with keywords should be valid");
    }

    @Test
    void testValidate_WithCategory() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCategory("Electronics");

        assertTrue(criteria.validate(),
                "Criteria with category should be valid");
    }

    @Test
    void testValidate_WithLocation() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setLocation("Library");

        assertTrue(criteria.validate(),
                "Criteria with location should be valid");
    }

    @Test
    void testValidate_WithType() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setType(ItemType.LOST);

        assertTrue(criteria.validate(),
                "Criteria with type should be valid");
    }

    @Test
    void testSettersAndGetters() {
        SearchCriteria criteria = new SearchCriteria();
        LocalDateTime now = LocalDateTime.now();

        criteria.setKeywords("phone");
        criteria.setCategory("Electronics");
        criteria.setLocation("Cafeteria");
        criteria.setType(ItemType.FOUND);
        criteria.setDateFrom(now.minusDays(7));
        criteria.setDateTo(now);

        assertEquals("phone", criteria.getKeywords());
        assertEquals("Electronics", criteria.getCategory());
        assertEquals("Cafeteria", criteria.getLocation());
        assertEquals(ItemType.FOUND, criteria.getType());
        assertEquals(now.minusDays(7), criteria.getDateFrom());
        assertEquals(now, criteria.getDateTo());
    }

    @Test
    void testValidate_WithMultipleCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeywords("laptop");
        criteria.setCategory("Electronics");
        criteria.setLocation("Library");
        criteria.setType(ItemType.LOST);

        assertTrue(criteria.validate(),
                "Criteria with multiple fields should be valid");
    }
}