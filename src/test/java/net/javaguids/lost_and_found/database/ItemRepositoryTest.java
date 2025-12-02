package net.javaguids.lost_and_found.database;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Tests for ItemRepository - uses actual DB so test data may stick around
@DisplayName("ItemRepository Tests")
class ItemRepositoryTest {

    private ItemRepository repository;
    private String testItemId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        repository = ItemRepository.getInstance();
        // Use random IDs to avoid conflicts
        testItemId = "item-test-" + UUID.randomUUID().toString().substring(0, 8);
        testUserId = "user-test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        if (testItemId != null) {
            repository.deleteItem(testItemId);
        }
    }

    @Test
    @DisplayName("Singleton instance is reused")
    void testSingletonInstance() {
        ItemRepository repo1 = ItemRepository.getInstance();
        ItemRepository repo2 = ItemRepository.getInstance();
        assertNotNull(repo1);
        assertSame(repo1, repo2, "ItemRepository should implement Singleton pattern");
    }

    @Test
    @DisplayName("Test getInstance returns non-null instance")
    void testGetInstanceReturnsNonNull() {
        ItemRepository instance = ItemRepository.getInstance();
        assertNotNull(instance, "ItemRepository instance should not be null");
    }

    @Test
    @DisplayName("Test saveItem saves a LostItem successfully")
    void testSaveLostItem() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Lost Wallet",
            "Brown leather wallet with cards",
            "Accessories",
            "Library",
            testUserId,
            LocalDateTime.now().minusDays(1),
            25.0
        );

        // Act
        boolean result = repository.saveItem(item);

        // Assert
        assertTrue(result, "saveItem should return true on success");
        
        // Check it was actually saved
        ItemRepository.Item savedItem = repository.getItemById(testItemId);
        assertNotNull(savedItem, "Saved item should be retrievable");
        assertTrue(savedItem instanceof ItemRepository.LostItem);
        assertEquals("Lost Wallet", savedItem.getTitle());
    }

    @Test
    @DisplayName("Test saveItem saves a FoundItem successfully")
    void testSaveFoundItem() {
        // Arrange
        ItemRepository.FoundItem item = new ItemRepository.FoundItem(
            testItemId,
            "Found Phone",
            "Black smartphone found",
            "Electronics",
            "Cafeteria",
            testUserId,
            LocalDateTime.now().minusHours(5)
        );

        // Act
        boolean result = repository.saveItem(item);

        // Assert
        assertTrue(result, "saveItem should return true on success");
        
        ItemRepository.Item savedItem = repository.getItemById(testItemId);
        assertNotNull(savedItem, "Saved item should be retrievable");
        assertTrue(savedItem instanceof ItemRepository.FoundItem);
        assertEquals("Found Phone", savedItem.getTitle());
    }

    @Test
    @DisplayName("Test getItemById retrieves existing item")
    void testGetItemById_ItemExists() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Test Item",
            "Test description",
            "Other",
            "Test Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            10.0
        );
        repository.saveItem(item);

        // Act
        ItemRepository.Item retrievedItem = repository.getItemById(testItemId);

        // Assert
        assertNotNull(retrievedItem, "Item should be found");
        assertEquals(testItemId, retrievedItem.getItemId());
        assertEquals("Test Item", retrievedItem.getTitle());
    }

    @Test
    @DisplayName("Test getItemById returns null for non-existent item")
    void testGetItemById_ItemNotExists() {
        // Act
        ItemRepository.Item item = repository.getItemById("non-existent-id");

        // Assert
        assertNull(item, "Should return null for non-existent item");
    }

    @Test
    @DisplayName("Test updateItem updates existing item")
    void testUpdateItem() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Original Title",
            "Original description",
            "Other",
            "Original Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            10.0
        );
        repository.saveItem(item);

        // Update the item
        item.setStatus(ItemRepository.ItemStatus.RESOLVED);
        item.setImagePath("/path/to/image.jpg");

        // Act
        boolean result = repository.updateItem(item);

        // Assert
        assertTrue(result, "updateItem should return true on success");
        
        ItemRepository.Item updatedItem = repository.getItemById(testItemId);
        assertNotNull(updatedItem);
        assertEquals(ItemRepository.ItemStatus.RESOLVED, updatedItem.getStatus());
        assertEquals("/path/to/image.jpg", updatedItem.getImagePath());
    }

    @Test
    @DisplayName("Test deleteItem removes item from database")
    void testDeleteItem() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Item to Delete",
            "This will be deleted",
            "Other",
            "Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );
        repository.saveItem(item);

        // Act
        boolean result = repository.deleteItem(testItemId);

        // Assert
        assertTrue(result, "deleteItem should return true on success");
        
        // Make sure it's actually gone
        ItemRepository.Item deletedItem = repository.getItemById(testItemId);
        assertNull(deletedItem, "Item should be deleted");
    }

    @Test
    @DisplayName("Test getItemsByUser retrieves items for a user")
    void testGetItemsByUser() {
        // Arrange
        ItemRepository.LostItem item1 = new ItemRepository.LostItem(
            testItemId,
            "Item 1",
            "Description 1",
            "Other",
            "Location 1",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );
        String item2Id = "item-test-2-" + UUID.randomUUID().toString().substring(0, 8);
        ItemRepository.FoundItem item2 = new ItemRepository.FoundItem(
            item2Id,
            "Item 2",
            "Description 2",
            "Other",
            "Location 2",
            testUserId,
            LocalDateTime.now().minusHours(5)
        );
        repository.saveItem(item1);
        repository.saveItem(item2);

        // Act
        List<ItemRepository.Item> items = repository.getItemsByUser(testUserId);

        // Assert
        assertNotNull(items, "Items list should not be null");
        assertTrue(items.size() >= 2, "Should retrieve at least 2 items");
        
        // Clean up
        repository.deleteItem(item2Id);
    }

    @Test
    @DisplayName("Test getItemsByUser returns empty list for user with no items")
    void testGetItemsByUser_NoItems() {
        // Arrange
        String nonExistentUserId = "non-existent-" + UUID.randomUUID().toString();

        // Act
        List<ItemRepository.Item> items = repository.getItemsByUser(nonExistentUserId);

        // Assert
        assertNotNull(items, "Items list should not be null");
        assertTrue(items.isEmpty(), "Should return empty list for user with no items");
    }

    @Test
    @DisplayName("Test searchItems with keyword criteria")
    void testSearchItems_Keyword() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Blue Backpack",
            "Blue backpack with laptop",
            "Bags",
            "Campus Library",
            testUserId,
            LocalDateTime.now().minusDays(1),
            50.0
        );
        repository.saveItem(item);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .keyword("backpack")
            .build();

        // Act
        List<ItemRepository.Item> results = repository.searchItems(criteria);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find item matching keyword");
    }

    @Test
    @DisplayName("Test searchItems with location criteria")
    void testSearchItems_Location() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Phone",
            "Black phone case",
            "Electronics",
            "Cafeteria",
            testUserId,
            LocalDateTime.now().minusHours(6),
            0.0
        );
        repository.saveItem(item);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .location("Cafeteria")
            .build();

        // Act
        List<ItemRepository.Item> results = repository.searchItems(criteria);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find item matching location");
    }

    @Test
    @DisplayName("Test searchItems with status criteria")
    void testSearchItems_Status() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Wallet",
            "Brown leather wallet",
            "Accessories",
            "Gym",
            testUserId,
            LocalDateTime.now().minusDays(2),
            20.0
        );
        item.setStatus(ItemRepository.ItemStatus.RESOLVED);
        repository.saveItem(item);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .status(ItemRepository.ItemStatus.RESOLVED)
            .build();

        // Act
        List<ItemRepository.Item> results = repository.searchItems(criteria);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find item matching status");
    }

    @Test
    @DisplayName("Test searchItems with type criteria")
    void testSearchItems_Type() {
        // Arrange
        ItemRepository.LostItem lostItem = new ItemRepository.LostItem(
            testItemId,
            "Lost Item",
            "Description",
            "Other",
            "Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );
        repository.saveItem(lostItem);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .type(ItemRepository.ItemType.LOST)
            .build();

        // Act
        List<ItemRepository.Item> results = repository.searchItems(criteria);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find lost item");
    }

    @Test
    @DisplayName("Test searchItems with combined criteria")
    void testSearchItems_Combined() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Test Item",
            "Test description",
            "Electronics",
            "Library",
            testUserId,
            LocalDateTime.now().minusDays(1),
            15.0
        );
        item.setStatus(ItemRepository.ItemStatus.ACTIVE);
        repository.saveItem(item);

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .keyword("Test")
            .location("Library")
            .status(ItemRepository.ItemStatus.ACTIVE)
            .type(ItemRepository.ItemType.LOST)
            .build();

        // Act
        List<ItemRepository.Item> results = repository.searchItems(criteria);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find item matching all criteria");
    }

    @Test
    @DisplayName("Test searchItems with null criteria returns all items")
    void testSearchItems_NullCriteria() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Any Item",
            "Any description",
            "Other",
            "Any Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );
        repository.saveItem(item);

        // Act
        List<ItemRepository.Item> results = repository.searchItems(null);

        // Assert
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.stream().anyMatch(i -> i.getItemId().equals(testItemId)),
            "Should find item with null criteria");
    }

    @Test
    @DisplayName("Test LostItem has correct properties")
    void testLostItemProperties() {
        // Arrange
        LocalDateTime dateLost = LocalDateTime.now().minusDays(2);
        double reward = 50.0;
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Lost Item",
            "Description",
            "Category",
            "Location",
            testUserId,
            dateLost,
            reward
        );

        // Assert
        assertEquals(testItemId, item.getItemId());
        assertEquals("Lost Item", item.getTitle());
        assertEquals(dateLost, item.getDateLost());
        assertEquals(reward, item.getReward());
        assertEquals(ItemRepository.ItemType.LOST, item.getType());
        assertEquals(ItemRepository.ItemStatus.ACTIVE, item.getStatus());
    }

    @Test
    @DisplayName("Test FoundItem has correct properties")
    void testFoundItemProperties() {
        // Arrange
        LocalDateTime dateFound = LocalDateTime.now().minusHours(3);
        ItemRepository.FoundItem item = new ItemRepository.FoundItem(
            testItemId,
            "Found Item",
            "Description",
            "Category",
            "Location",
            testUserId,
            dateFound
        );

        // Assert
        assertEquals(testItemId, item.getItemId());
        assertEquals("Found Item", item.getTitle());
        assertEquals(dateFound, item.getDateFound());
        assertEquals(ItemRepository.ItemType.FOUND, item.getType());
        assertEquals(ItemRepository.ItemStatus.ACTIVE, item.getStatus());
    }

    @Test
    @DisplayName("Test item status can be changed")
    void testItemStatusChange() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Test Item",
            "Description",
            "Other",
            "Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );

        // Act
        item.setStatus(ItemRepository.ItemStatus.RESOLVED);

        // Assert
        assertEquals(ItemRepository.ItemStatus.RESOLVED, item.getStatus());
    }

    @Test
    @DisplayName("Test item image path can be set")
    void testItemImagePath() {
        // Arrange
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Test Item",
            "Description",
            "Other",
            "Location",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );

        // Act
        item.setImagePath("/images/test.jpg");

        // Assert
        assertEquals("/images/test.jpg", item.getImagePath());
    }

    @Test
    @DisplayName("Test SearchCriteria builder pattern")
    void testSearchCriteriaBuilder() {
        // Act
        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .keyword("test")
            .location("Library")
            .status(ItemRepository.ItemStatus.ACTIVE)
            .type(ItemRepository.ItemType.LOST)
            .build();

        // Assert
        assertEquals("test", criteria.getKeyword());
        assertEquals("Library", criteria.getLocation());
        assertEquals(ItemRepository.ItemStatus.ACTIVE, criteria.getStatus());
        assertEquals(ItemRepository.ItemType.LOST, criteria.getType());
    }

    @Test
    @DisplayName("Test LostItem matches keyword criteria")
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
    @DisplayName("Test SearchCriteria filters by location and status")
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
    @DisplayName("Test SearchCriteria respects type filtering")
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

    @Test
    @DisplayName("Test item does not match when keyword doesn't match")
    void testItemDoesNotMatchKeyword() {
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Wallet",
            "Brown wallet",
            "Accessories",
            "Gym",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .keyword("phone")
            .build();

        assertFalse(item.matches(criteria), "Item should not match when keyword doesn't match");
    }

    @Test
    @DisplayName("Test item does not match when location doesn't match")
    void testItemDoesNotMatchLocation() {
        ItemRepository.LostItem item = new ItemRepository.LostItem(
            testItemId,
            "Item",
            "Description",
            "Other",
            "Library",
            testUserId,
            LocalDateTime.now().minusDays(1),
            0.0
        );

        ItemRepository.SearchCriteria criteria = new ItemRepository.SearchCriteria.Builder()
            .location("Cafeteria")
            .build();

        assertFalse(item.matches(criteria), "Item should not match when location doesn't match");
    }
}
