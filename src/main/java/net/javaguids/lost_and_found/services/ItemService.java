package net.javaguids.lost_and_found.services;

import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.search.SearchCriteria;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.utils.ValidationUtil;
import net.javaguids.lost_and_found.analytics.ActivityLog;

import java.util.List;

public class ItemService {
    private ItemRepository itemRepository;

    public ItemService() {
        this.itemRepository = ItemRepository.getInstance();
    }

    public boolean postItem(Item item) {
        if (!ValidationUtil.isValidItem(item)) {
            return false;
        }

        boolean success = itemRepository.saveItem(item);
        if (success) {
            ActivityLog.log(item.getPostedByUserId(), "POST_ITEM", "Posted item: " + item.getTitle());
        }
        return success;
    }

    public List<Item> searchItems(SearchCriteria criteria) {
        return itemRepository.searchItems(criteria);
    }

    public List<Item> searchItemsExcludingUser(SearchCriteria criteria, String userId) {
        List<Item> allItems = itemRepository.searchItems(criteria);
        List<Item> filteredItems = new java.util.ArrayList<>();

        // Go through each item and keep only items that match our criteria
        for (Item item : allItems) {
            // Skip items posted by the current user
            if (item.getPostedByUserId().equals(userId)) {
                continue;
            }

            // Skip items that are RESOLVED
            if (item.getStatus() == net.javaguids.lost_and_found.model.enums.ItemStatus.RESOLVED) {
                continue;
            }

            // Add the item if it passed both checks
            filteredItems.add(item);
        }

        return filteredItems;
    }

    public Item getItemById(String itemId) {
        return itemRepository.getItemById(itemId);
    }

    public List<Item> getItemsByUser(String userId) {
        return itemRepository.getItemsByUser(userId);
    }

    public boolean updateItem(Item item) {
        boolean success = itemRepository.updateItem(item);
        if (success) {
            ActivityLog.log(item.getPostedByUserId(), "UPDATE_ITEM", "Updated item: " + item.getTitle());
        }
        return success;
    }

    public boolean deleteItem(String itemId) {
        Item item = itemRepository.getItemById(itemId);
        boolean success = itemRepository.deleteItem(itemId);
        if (success && item != null) {
            ActivityLog.log(item.getPostedByUserId(), "DELETE_ITEM", "Deleted item: " + item.getTitle());
        }
        return success;
    }
}