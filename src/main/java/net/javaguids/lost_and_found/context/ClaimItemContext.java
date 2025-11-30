package net.javaguids.lost_and_found.context;

import net.javaguids.lost_and_found.model.items.Item;

/**
 * Context for passing item data to claim view
 */
public class ClaimItemContext {
    private static Item itemToClaim;

    public static void setItem(Item item) {
        itemToClaim = item;
    }

    public static Item getItem() {
        return itemToClaim;
    }

    public static void clear() {
        itemToClaim = null;
    }
}