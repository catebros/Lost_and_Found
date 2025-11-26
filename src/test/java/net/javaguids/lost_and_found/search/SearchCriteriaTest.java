package net.javaguids.lost_and_found.search;

import net.javaguids.lost_and_found.model.enums.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {
    private SearchCriteria criteria;

    @BeforeEach
    void setUp() {
        criteria = new SearchCriteria();
    }

    @Test
    @DisplayName("Create search criteria and verify default values")
    void testDefaultConstructor() {
        assertNotNull(criteria);
        assertNull(criteria.getKeywords());
        assertNull(criteria.getCategory());
        assertNull(criteria.getLocation());
        assertNull(criteria.getType());
        assertNull(criteria.getDateFrom());
        assertNull(criteria.getDateTo());
    }

    @Test
    @DisplayName("Set and get all fields")
    void testSettersAndGetters() {
        LocalDateTime dateFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 12, 31, 23, 59);

        criteria.setKeywords("wallet");
        criteria.setCategory("Electronics");
        criteria.setLocation("Library");
        criteria.setType(ItemType.LOST);
        criteria.setDateFrom(dateFrom);
        criteria.setDateTo(dateTo);

        assertEquals("wallet", criteria.getKeywords());
        assertEquals("Electronics", criteria.getCategory());
        assertEquals("Library", criteria.getLocation());
        assertEquals(ItemType.LOST, criteria.getType());
        assertEquals(dateFrom, criteria.getDateFrom());
        assertEquals(dateTo, criteria.getDateTo());
    }

    @Test
    @DisplayName("Validate criteria with different field combinations")
    void testValidate() {
        assertFalse(criteria.validate());

        criteria.setKeywords("wallet");
        assertTrue(criteria.validate());

        criteria = new SearchCriteria();
        criteria.setCategory("Electronics");
        assertTrue(criteria.validate());

        criteria = new SearchCriteria();
        criteria.setLocation("Library");
        assertTrue(criteria.validate());

        criteria = new SearchCriteria();
        criteria.setType(ItemType.LOST);
        assertTrue(criteria.validate());
    }
}
