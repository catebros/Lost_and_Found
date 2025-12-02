package net.javaguids.lost_and_found.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import net.javaguids.lost_and_found.analytics.Statistics; 
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.utils.FileHandler;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.EditItemContext;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.utils.PasswordUtil;
import net.javaguids.lost_and_found.context.EditUserContext;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// Contoller for the admin dashboard view
// Functionalities: user and item management tables with edit/delete actions, activity logs display with export functionality

public class AdminDashboardController {
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableView<Item> itemsTable;

    @FXML
    private TextArea logsArea; 

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalItemsLabel;

    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private MessageRepository messageRepository;

    // Initializes the controller, sets up tables, and loads data
    @FXML
    public void initialize() {
        // initialize repositories for data access
        userRepository = UserRepository.getInstance();
        itemRepository = ItemRepository.getInstance();
        messageRepository = MessageRepository.getInstance();

        // configure table structures and columns
        setupUsersTable();
        setupItemsTable();

        // load initial data into tables and logs
        loadUsers();
        loadItems();
        loadLogs();
        loadStatistics();
    }

    // Configures the users table with columns and action buttons
    private void setupUsersTable() {
        // clear existing columns to avoid duplication
        usersTable.getColumns().clear();

        // user id column
        TableColumn<User, String> userIdCol = new TableColumn<>("User ID");
        userIdCol.setPrefWidth(120);
        userIdCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserId()));

        // username column 
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setPrefWidth(130);
        usernameCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));

        // email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setPrefWidth(180);
        emailCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));

        // role column (USER/MODERATOR/ADMIN)
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setPrefWidth(80);
        roleCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().toString()));

        // Actions column with Edit and Delete buttons for each user
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);

            {
                // configure edit button action
                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                // configure delete button action and style
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // only show buttons for non-empty rows
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        // add all configured columns to table
        usersTable.getColumns().addAll(userIdCol, usernameCol, emailCol, roleCol, actionsCol);
    }

    // Configures the items table with columns and action buttons
    private void setupItemsTable() {
        // clear existing columns to avoid duplication
        itemsTable.getColumns().clear();

        // item title column
        TableColumn<Item, String> titleCol = new TableColumn<>("Title");
        titleCol.setPrefWidth(120);
        titleCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

        // item category column
        TableColumn<Item, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setPrefWidth(90);
        categoryCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));

        // item location column (where it was lost/found)
        TableColumn<Item, String> locationCol = new TableColumn<>("Location");
        locationCol.setPrefWidth(100);
        locationCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));

        // posted by (username of the user who posted the item) column
        TableColumn<Item, String> userCol = new TableColumn<>("Posted By");
        userCol.setPrefWidth(100);
        userCol.setCellValueFactory(cellData -> {
            // resolve user ID to username for readability
            String userId = cellData.getValue().getPostedByUserId();
            User user = userRepository.getUserById(userId);
            String username = user != null ? user.getUsername() : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(username);
        });

        //type column (LOST/FOUND)
        TableColumn<Item, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(60);
        typeCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));

        // status column (OPEN/CLAIMED/RETURNED)
        TableColumn<Item, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(80);
        statusCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));

        // Actions column with Edit and Delete buttons for each item
        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(param -> new TableCell<Item, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);

            {
                // configure edit button action
                editBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    handleEditItem(item);
                });

                // configure delete button action and style
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // only show buttons for non-empty rows
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        // add all configured columns to table
        itemsTable.getColumns().addAll(titleCol, categoryCol, locationCol, userCol, typeCol, statusCol, actionsCol);
    }

    // Loads activity logs from the repository and displays them in the logs area
    private void loadLogs() {
        // define time range for log retrieval (last 30 days)
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();
        List<ActivityLog> logs = messageRepository.getActivityLogs(from, to);

        // build formatted log text for display
        StringBuilder logsText = new StringBuilder();
        logsText.append("Activity Logs (Last 30 Days)\n");
        logsText.append("=".repeat(50)).append("\n\n");

        // process each log entry with username 
        for (ActivityLog log : logs) {
            String userName;
            if (log.getUserId().equals("SYSTEM")) {
                userName = "SYSTEM";
            } else {
                // resolve user ID to username for better readability
                User user = userRepository.getUserById(log.getUserId());
                userName = user != null ? user.getUsername() : "Unknown User";
            }

            // format log entry: timestamp - [username] action: details
            logsText.append(log.getTimestamp()).append(" - ")
                    .append("[").append(userName).append("] ")
                    .append(log.getAction()).append(": ")
                    .append(log.getDetails()).append("\n");
        }

        // display formatted logs in the text area
        logsArea.setText(logsText.toString());
    }

    // handles exporting activity logs to a CSV file
    @FXML
    public void handleExportLogs() {
        // configure file chooser for CSV export
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // show save dialog
        Stage stage = (Stage) logsArea.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // retrieve logs for the last 30 days
            LocalDateTime from = LocalDateTime.now().minusDays(30);
            LocalDateTime to = LocalDateTime.now();
            List<ActivityLog> logs = messageRepository.getActivityLogs(from, to);

            // attempt to export logs to the selected CSV file
            boolean success = FileHandler.exportLogsToCSV(logs, file.getAbsolutePath());
            if (success) {
                AlertUtil.showAlert("Success", "Logs exported successfully", Alert.AlertType.INFORMATION);
            } else {
                AlertUtil.showAlert("Error", "Failed to export logs", Alert.AlertType.ERROR);
            }
        }
    }

    // Handles viewing statistics 
    @FXML
    public void handleViewStatistics() {
        //Statistics stats = messageRepository.generateStatistics();
        //AlertUtil.showAlert("Statistics", stats.generateReport(), Alert.AlertType.INFORMATION);
    }

    // Handles going back to the previous view using NavigationManager
    @FXML
    public void handleGoBack() {
        NavigationManager.goBack();
    }

    // Handles the logout action
    // clears the current session and navigates back to the login view
    @FXML
    public void handleLogout() {
        AuthService.logout();
        NavigationManager.navigateTo("login-view.fxml", "Lost and Found - Login");
    }

    private void handleDeleteUser(User user) {
        if (user != null) {
            // Safety check: prevent admins from deleting their own account
            User currentUser = AuthService.getCurrentUser();
            if (currentUser != null && currentUser.getUserId().equals(user.getUserId())) {
                AlertUtil.showAlert("Error", "You cannot delete your own account", Alert.AlertType.ERROR);
                return;
            }

            // show confirmation dialog before deletion
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete User");
            confirmation.setContentText("Are you sure you want to delete user: " + user.getUsername() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // perform deletion and refresh data upon success
                    boolean success = userRepository.deleteUser(user.getUserId());
                    if (success) {
                        AlertUtil.showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                        // refresh all related data
                        loadUsers();
                        loadItems(); // items might be affected by user deletion
                        loadStatistics();
                    } else {
                        AlertUtil.showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    // Handles deleting an item after confirmation
    private void handleDeleteItem(Item item) {
        if (item != null) {
            // show confirmation dialog before deletion
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Item");
            confirmation.setContentText("Are you sure you want to delete item: " + item.getTitle() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // delete and refresh data
                    boolean success = itemRepository.deleteItem(item.getItemId());
                    if (success) {
                        AlertUtil.showAlert("Success", "Item deleted successfully", Alert.AlertType.INFORMATION);
                        loadItems(); // refresh items table
                    } else {
                        AlertUtil.showAlert("Error", "Failed to delete item", Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }
    
    // load all users from the database and populate the users table
    // convert the user list to an observable list for JavaFX table compatibility
    private void loadUsers() {
        List<User> users = userRepository.getAllUsers();
        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        usersTable.setItems(observableUsers);
    }


    // load all items from the database and populate the items table
    private void loadItems() {
        List<Item> items = itemRepository.searchItems(null);
        ObservableList<Item> observableItems = FXCollections.observableArrayList(items);
        itemsTable.setItems(observableItems);
    }

    // load statistics such as total users and total items
    private void loadStatistics() {
        Statistics stats = messageRepository.generateStatistics();
        totalUsersLabel.setText("Total Users: " + stats.getTotalUsers());
        totalItemsLabel.setText("Total Items: " + stats.getTotalItems());
    }

    // Handles the create user button click to navigate to the create user view
    @FXML
    public void handleCreateUser() {
        NavigationContext.setPreviousPage("admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard");
        NavigationManager.navigateTo("create-user-view.fxml", "Create New User");
    }

    // Handles the create item button click to navigate to the create item view
    @FXML
    public void handleCreateItem() {
        // Safety check: ensure at least one user exists before allowing to create items
        List<User> allUsers = userRepository.getAllUsers();
        if (allUsers.isEmpty()) {
            AlertUtil.showAlert("No Users Available", "You must create at least one user before creating items.", Alert.AlertType.WARNING);
            return;
        }

        NavigationContext.setPreviousPage("admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard");
        NavigationManager.navigateTo("post-item-view.fxml", "Create New Item");
    }

    // Handles editing a user by navigating to the edit user view with the selected user's data
    private void handleEditUser(User user) {
        if (user == null) return;

        // Safety checL prevent admins from changing their own role 
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getUserId().equals(user.getUserId())) {
            AlertUtil.showAlert("Error", "You cannot change your own role", Alert.AlertType.ERROR);
            return;
        }

        // Set up edit context and navigate to edit form
        EditUserContext.setUser(user);
        NavigationContext.setPreviousPage("admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard");
        NavigationManager.navigateTo("edit-user-view.fxml", "Edit User");
    }

    // Handles editing an item by navigating to the edit item view with the selected item's data
    private void handleEditItem(Item item) {
        if (item == null) return;

        // Set up edit context and navigate to edit form
        EditItemContext.setItem(item);
        NavigationContext.setPreviousPage("admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard");
        NavigationManager.navigateTo("post-item-view.fxml", "Edit Item");
    }
}