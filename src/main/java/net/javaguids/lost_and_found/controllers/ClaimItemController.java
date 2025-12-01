package net.javaguids.lost_and_found.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.ClaimItemContext;
import net.javaguids.lost_and_found.context.NavigationContext;

import java.util.List;

public class ClaimItemController {
    @FXML
    private ComboBox<String> usersComboBox;

    @FXML
    private ComboBox<ItemDisplayWrapper> itemsComboBox;

    @FXML
    private Label selectedItemLabel;

    private Item currentItem;
    private UserDashboardController parentController;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private MessageRepository messageRepository;
    private ItemService itemService;
    private User currentUser;

    // Wrapper class to display item info
    public static class ItemDisplayWrapper {
        private Item item;

        public ItemDisplayWrapper(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public String toString() {
            if (item == null) {
                return "No published item";
            }
            return item.getTitle() + " (" + item.getType() + ")";
        }
    }

    @FXML
    public void initialize() {
        userRepository = UserRepository.getInstance();
        itemRepository = ItemRepository.getInstance();
        messageRepository = MessageRepository.getInstance();
        itemService = new ItemService();
        currentUser = AuthService.getCurrentUser();

        // Load conversation users when user selection changes
        usersComboBox.setOnAction(e -> loadUserItems());

        // Handle item selection changes
        itemsComboBox.setOnAction(e -> {
            ItemDisplayWrapper selected = itemsComboBox.getValue();
            if (selected != null && selected.getItem() != null) {
                selectedItemLabel.setText("Selected: " + selected.toString());
            } else {
                selectedItemLabel.setText("Selected: No published item");
            }
        });

        // Check if this is claim mode from context
        Item itemToClaim = ClaimItemContext.getItem();
        if (itemToClaim != null) {
            setItem(itemToClaim, null);
            ClaimItemContext.clear();
        }
    }

    public void setItem(Item item, UserDashboardController parentController) {
        this.currentItem = item;
        this.parentController = parentController;
        loadConversationUsers();
    }

    private void loadConversationUsers() {
        // Get all users from conversations related to current item
        List<String> conversationUserIds = messageRepository.getUsersFromConversations(currentUser.getUserId());

        ObservableList<String> userNames = FXCollections.observableArrayList();

        for (String userId : conversationUserIds) {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                userNames.add(user.getUsername());
            }
        }

        usersComboBox.setItems(userNames);

        if (!userNames.isEmpty()) {
            usersComboBox.getSelectionModel().selectFirst();
            loadUserItems();
        }
    }

    private void loadUserItems() {
        String selectedUserName = usersComboBox.getValue();
        if (selectedUserName == null) {
            return;
        }

        // Get user ID from username
        User selectedUser = userRepository.getUserByUsername(selectedUserName);
        if (selectedUser == null) {
            return;
        }

        // Get all items posted by the selected user
        List<Item> userItems = itemService.getItemsByUser(selectedUser.getUserId());

        ObservableList<ItemDisplayWrapper> itemWrappers = FXCollections.observableArrayList();

        // Add "No published item" option
        itemWrappers.add(new ItemDisplayWrapper(null));

        // Determine the opposite type to filter by
        // If current item is LOST, we want to see FOUND items from other users, and vice versa
        net.javaguids.lost_and_found.model.enums.ItemType oppositeType = null;
        if (currentItem != null) {
            if (currentItem.getType() == net.javaguids.lost_and_found.model.enums.ItemType.LOST) {
                oppositeType = net.javaguids.lost_and_found.model.enums.ItemType.FOUND;
            } else if (currentItem.getType() == net.javaguids.lost_and_found.model.enums.ItemType.FOUND) {
                oppositeType = net.javaguids.lost_and_found.model.enums.ItemType.LOST;
            }
        }

        // Add user's items that are not resolved and match the opposite type
        for (Item item : userItems) {
            if (!item.getStatus().equals(net.javaguids.lost_and_found.model.enums.ItemStatus.RESOLVED)) {
                // Only add items of the opposite type
                if (oppositeType != null && item.getType() == oppositeType) {
                    itemWrappers.add(new ItemDisplayWrapper(item));
                }
            }
        }

        itemsComboBox.setItems(itemWrappers);
        itemsComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleGoBack() {
        String previousPage = NavigationContext.getPreviousPage();
        String previousTitle = NavigationContext.getPreviousTitle();
        NavigationContext.clear();

        if (previousPage != null && previousTitle != null) {
            NavigationManager.navigateTo(previousPage, previousTitle);
        } else {
            // Fallback to dashboard if no previous page stored
            NavigationManager.goBack();
        }
    }

    @FXML
    public void handleClaim() {
        String selectedUserString = usersComboBox.getValue();
        ItemDisplayWrapper selectedItemWrapper = itemsComboBox.getValue();

        if (selectedUserString == null) {
            AlertUtil.showAlert("Error", "Please select a user", Alert.AlertType.ERROR);
            return;
        }

        if (selectedItemWrapper == null) {
            AlertUtil.showAlert("Error", "Please select an item", Alert.AlertType.ERROR);
            return;
        }

        try {
            //TODO:FIX
            // Mark current item as resolved
            //currentItem.setStatus(net.javaguids.lost_and_found.model.enums.ItemStatus.RESOLVED);
            //boolean itemUpdated = itemRepository.updateItem(currentItem);

            //if (!itemUpdated) {
           //     AlertUtil.showAlert("Error", "Failed to update current item", Alert.AlertType.ERROR);
            //    return;
            //}

            // If a matching item was selected, mark it as resolved too
            if (selectedItemWrapper.getItem() != null) {
                Item matchingItem = selectedItemWrapper.getItem();
                matchingItem.setStatus(net.javaguids.lost_and_found.model.enums.ItemStatus.RESOLVED);
                //TODO: FIX
                //boolean matchingItemUpdated = itemRepository.updateItem(matchingItem);

                //if (!matchingItemUpdated) {
                //    AlertUtil.showAlert("Error", "Failed to update matching item", Alert.AlertType.ERROR);
                 //   return;
                //}
            }

            AlertUtil.showAlert("Success", "Item claimed successfully!", Alert.AlertType.INFORMATION);

            // Refresh parent controller's items list
            if (parentController != null) {
                parentController.loadMyItems();
            }

            // Navigate back to dashboard
            NavigationManager.goBack();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Failed to claim item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}