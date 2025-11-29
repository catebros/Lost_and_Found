package net.javaguids.lost_and_found.context;

import net.javaguids.lost_and_found.model.items.Item;

// Static context class for managing item data when viewing item details.
public class ItemDetailsContext {
    // item being viewed stored as static for global access
    private static Item currentItem;

    // Sets the item to be displayed in view details
    public static void setItem(Item item) {
        currentItem = item;
    }

    // Retrieves the item currently stored in the details context.
    public static Item getItem() {
        return currentItem;
    }

    // Clears the details context by setting the current item to null.
    public static void clear() {
        currentItem = null;
    }
}
