package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.search.SearchCriteria;
// TODO: Uncomment when ItemRepository is implemented (Commit 8)
// import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.utils.ValidationUtil;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import net.javaguids.lost_and_found.model.enums.ItemStatus;

import java.util.List;
import java.util.ArrayList;

// Service layer for item-related business logic.

public class ItemService {

    // TODO: Uncomment when ItemRepository is implemented
    // private ItemRepository itemRepository;

    // Constructor initializes the item repository
    public ItemService() {
        // TODO: Uncomment when ItemRepository is implemented
        // this.itemRepository = ItemRepository.getInstance();
    }

    // Posts a new item after validation. Validates item data and logs the action if successful
    public boolean postItem(Item item) {
        // Validate item has all required fields
        if (!ValidationUtil.isValidItem(item)) {
            return false;
        }

        // TODO: Uncomment when ItemRepository is implemented
        // boolean success = itemRepository.saveItem(item);
        // if (success) {
        //     ActivityLog.log(item.getPostedByUserId(), "POST_ITEM", "Posted item: " + item.getTitle());
        // }
        // return success;

        return true;
    }

    // Searches for items matching the given criteria.
    public List<Item> searchItems(SearchCriteria criteria) {
        // TODO: Uncomment when ItemRepository is implemented
        // return itemRepository.searchItems(criteria);

        // Temporary: Return empty list
        return new ArrayList<>();
    }

    // Searches for items excluding those posted by the specified user. Also filters out RESOLVED items

    public List<Item> searchItemsExcludingUser(SearchCriteria criteria, String userId) {

        // TODO: Uncomment when ItemRepository is implemented
        // List<Item> allItems = itemRepository.searchItems(criteria);
        List<Item> allItems = new ArrayList<>(); // Temporary

        List<Item> filteredItems = new ArrayList<>();

        // Filter items based on user and status
        for (Item item : allItems) {
            // Skip items posted by the current user
            if (item.getPostedByUserId().equals(userId)) {
                continue;
            }

            // Skip items that are already resolved
            if (item.getStatus() == ItemStatus.RESOLVED) {
                continue;
            }

            // Add the item if it passed both checks
            filteredItems.add(item);
        }

        return filteredItems;
    }

    // Retrieves a single item by its ID.

    public Item getItemById(String itemId) {
        // TODO: Uncomment when ItemRepository is implemented
        // return itemRepository.getItemById(itemId);

        // Temporary: Return null
        return null;
    }

    // Retrieves all items posted by a specific user.

    public List<Item> getItemsByUser(String userId) {
        // TODO: Uncomment when ItemRepository is implemented
        // return itemRepository.getItemsByUser(userId);

        // Temporary: Return empty list
        return new ArrayList<>();
    }

    // Updates an existing item and logs the action.
    public boolean updateItem(Item item) {
        // TODO: Uncomment when ItemRepository is implemented
        // boolean success = itemRepository.updateItem(item);
        // if (success) {
        //     ActivityLog.log(item.getPostedByUserId(), "UPDATE_ITEM", "Updated item: " + item.getTitle());
        // }
        // return success;

        // Temporary: Log action and return true for testing
        ActivityLog.log(item.getPostedByUserId(), "UPDATE_ITEM", "Updated item: " + item.getTitle());
        return true;
    }

    // Deletes an item and logs the action. Retrieves item details before deletion for logging purposes
    public boolean deleteItem(String itemId) {
        // TODO: Uncomment when ItemRepository is implemented
        // Item item = itemRepository.getItemById(itemId);
        // boolean success = itemRepository.deleteItem(itemId);
        // if (success && item != null) {
        //     ActivityLog.log(item.getPostedByUserId(), "DELETE_ITEM", "Deleted item: " + item.getTitle());
        // }
        // return success;

        // Temporary: Return true for testing
        System.out.println("DELETE_ITEM: " + itemId);
        return true;
    }
}