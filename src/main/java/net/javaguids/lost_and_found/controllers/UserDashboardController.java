package net.javaguids.lost_and_found.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.ItemService; 
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.EditItemContext; 
import net.javaguids.lost_and_found.context.ClaimItemContext; 
import net.javaguids.lost_and_found.context.NavigationContext;

import java.util.List;

// Controller for the user dashboard view
// Manages user's items with auto refresh and item management actions.

public class UserDashboardController {
    @FXML
    private ListView<Item> myItemsList;    // display user's items

    @FXML
    private Label usernameLabel;

    private ItemService itemService; // service for item operations
    private User currentUser; // currently logged-in user


    // Initializes the controller after FXML loading
    @FXML
    public void initialize() {
        // Initialize services and load user data
        itemService = new ItemService();
        currentUser = AuthService.getCurrentUser();

        // Only proeed if user is logged in
        if (currentUser != null) {
            usernameLabel.setText("Welcome, " + currentUser.getUsername());
            loadMyItems();

            // Auto-refresh items every 10 seconds
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
                loadMyItems();
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }


    // Handles Post Item button click - Navigates to the Post Item view.
    @FXML
    public void handlePostItem() {
        NavigationManager.navigateTo("post-item-view.fxml", "Lost and Found - Post Item");
    }

    // Handles Search button click - Navigates to the Search view.
    @FXML
    public void handleSearch() {
        NavigationManager.navigateTo("search-view.fxml", "Lost and Found - Search Items");
    }

    // Handles Messages button click - Navigates to the Messages view.
    @FXML
    public void handleMessages() {
        NavigationManager.navigateTo("messages-view.fxml", "Lost and Found - Messages");
    }

    // Handles Go Back button click - Navigates to the previous page.
    @FXML
    public void handleGoBack() {
        NavigationManager.goBack();
    }

    // Handles Logout button click - Logs out the user and navigates to the Login view.
    @FXML
    public void handleLogout() {
        AuthService.logout();
        NavigationManager.navigateTo("login-view.fxml", "Lost and Found - Login");
    }

    // Loads and displays the current user's items in the ListView.
    public void loadMyItems() {
        if (currentUser != null) {
            List<Item> items = itemService.getItemsByUser(currentUser.getUserId());
            ObservableList<Item> observableItems = FXCollections.observableArrayList(items);
            myItemsList.setItems(observableItems);

            myItemsList.setCellFactory(param -> new ListCell<Item>() {
                // Updates the cell content for each item in the ListView
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);

                    // handle empty cells
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Create item info label
                        Label itemLabel = new Label(item.getTitle() + " - " + item.getType() + " - " + item.getStatus());

                        javafx.scene.layout.HBox buttons;

                        // For resolved items, only show delete button
                        if (item.getStatus() == net.javaguids.lost_and_found.model.enums.ItemStatus.RESOLVED) {
                            Button deleteBtn = new Button("Delete");
                            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                            deleteBtn.setOnAction(e -> handleDeleteItem(item));

                            buttons = new javafx.scene.layout.HBox(5, deleteBtn);
                        } else {
                            // For active items, show all buttons
                            Button editBtn = new Button("Edit");
                            editBtn.setOnAction(e -> handleEditItem(item));

                            Button claimBtn = new Button("Claim");
                            claimBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                            claimBtn.setOnAction(e -> handleClaimItem(item));

                            Button deleteBtn = new Button("Delete");
                            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                            deleteBtn.setOnAction(e -> handleDeleteItem(item));

                            buttons = new javafx.scene.layout.HBox(5, editBtn, claimBtn, deleteBtn);
                        }

                        // Create spacer to push buttons to the right
                        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();

                        // Create main container with item label on left and buttons on right
                        javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10, itemLabel, spacer, buttons);

                        
                        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                        javafx.scene.layout.HBox.setHgrow(itemLabel, javafx.scene.layout.Priority.NEVER);
                        javafx.scene.layout.HBox.setHgrow(buttons, javafx.scene.layout.Priority.NEVER);
                        container.setStyle("-fx-alignment: center;");

                        setText(null);
                        setGraphic(container);
                    }
                }
            });
        }
    }

    // Sets up edit context and navigates to the post item view for editing.
    private void handleEditItem(Item item) {
        EditItemContext.setItem(item);
        NavigationContext.setPreviousPage("user-dashboard-view.fxml", "Lost and Found - Dashboard");
        NavigationManager.navigateTo("post-item-view.fxml", "Edit Item");
    }

    // Sets up claim context and navigates to the claim item view.
    private void handleClaimItem(Item item) {
        ClaimItemContext.setItem(item);
        NavigationContext.setPreviousPage("user-dashboard-view.fxml", "Lost and Found - Dashboard");
        NavigationManager.navigateTo("claim-item-view.fxml", "Claim Item");
    }

    // Handles deleting an item with user confirmation.
    private void handleDeleteItem(Item item) {
        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Item");
        confirmation.setContentText("Are you sure you want to delete this item?");

        // show dialog and handle response
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = itemService.deleteItem(item.getItemId());
                if (success) {
                    AlertUtil.showAlert("Success", "Item deleted successfully", Alert.AlertType.INFORMATION);
                    loadMyItems();
                } else {
                    AlertUtil.showAlert("Error", "Failed to delete item", Alert.AlertType.ERROR);
                }
            }
            // if user cancels, do nothing
        });
    }
}
