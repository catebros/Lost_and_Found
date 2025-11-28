package net.javaguids.lost_and_found.context;

import net.javaguids.lost_and_found.model.items.Item;


// Context for passing item data between different views/controllers when editing an item
// It uses a static field to temporaly store the item being edited
public class EditItemContext {
    // Item being edited
    private static Item itemToEdit;

    // Sets the item to be edited in the global context
    public static void setItem(Item item) {
        itemToEdit = item;
    }

    // Retrieves the item being edited from the global context
    public static Item getItem() {
        return itemToEdit;
    }

    // Clears the item being edited from the global context
    public static void clear() {
        itemToEdit = null;
    }
}
