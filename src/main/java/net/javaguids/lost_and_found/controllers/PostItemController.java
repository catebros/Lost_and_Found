package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.ItemService;
// import net.javaguids.lost_and_found.utils.FileHandler; TODO: Implement FileHandler util for image management operations (method: saveImage(File, String))
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.EditItemContext;
import net.javaguids.lost_and_found.context.NavigationContext;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Controller for the Post Item view
// This controller handles both creating new items and editing existing ones
// Features: create items, edit existing ones, validation, image upload and management, admin mode for user assignment 
// and status control, change detection in edit mde and navigation integration with context management.
public class PostItemController {
    // FXML UI COMPONENTS

    // Text field for item title
    @FXML
    private TextField titleField;

    // Text area for item description
    @FXML
    private TextArea descriptionArea;

    // Dropdown for selecting item category
    @FXML
    private ComboBox<String> categoryCombo;

    // Text field for entering item location
    @FXML
    private TextField locationField;

    // date picker for selecting lost/found date
    @FXML
    private DatePicker datePicker;

    // buttons for selecting lost item type
    @FXML
    private RadioButton lostRadio;

    // button for found item type
    @FXML
    private RadioButton foundRadio;

    // text field for entering reward amount (only for lost items)
    @FXML
    private TextField rewardField;

    // FMXL UI COMPONENTS FOR IMAGE HANDLING

    // Label showing selected image file name
    @FXML
    private Label imagePathLabel;

    // main title label that changes based on mode (POST Vs EDIT)
    @FXML
    private Label titleLabel;

    // button to remove selected image
    @FXML
    private Button removeImageButton;

    // ADMIN MODE UI COMPONENTS

    // Container for user selection controls
    @FXML
    private javafx.scene.layout.VBox userSelectionBox;

    // dropdown for selecting which user owns the item
    @FXML
    private ComboBox<String> userComboBox;

    // dcontainer for status selection controls
    @FXML
    private javafx.scene.layout.VBox statusSelectionBox;

    // dropdown for selecting item status
    @FXML
    private ComboBox<String> statusComboBox;


    // CONTROLLER STATE VARIABLES
    // Toggle group to ensure only one item type (Lost or found) is selected at a time
    private ToggleGroup typeGroup;
    // Handles item related operations
    private ItemService itemService;
    // path to the selected image file
    private String imagePath;
    // reference to item being edited (null if creating new item)
    private Item editingItem;
    // reference to dashboard controller for refreshing data after edit
    private UserDashboardController dashboardController;
    // flag indicating if the controller is in admin mode
    private boolean isAdminMode = false;

    // Original field values stored for change detection in edit mode
    private String originalTitle;
    private String originalDescription;
    private String originalCategory;
    private String originalLocation;
    private String originalDate;
    private String originalReward;
    private String originalType;
    private String originalImagePath;
    private String originalAssignedUser;
    private String originalStatus;


    // Initializes controller after FXML components are loaded
    // Sets up UI components, loads users/statuses for admin mode, and checks for edit mode context.
    @FXML
    public void initialize() {
        itemService = new ItemService();
        // Set up toggle group for item type radio buttons
        typeGroup = new ToggleGroup();
        lostRadio.setToggleGroup(typeGroup);
        foundRadio.setToggleGroup(typeGroup);
        
        //  populate catefory dropdown with predefined categories
        categoryCombo.getItems().addAll(
            "Electronics", "Clothing", "Accessories", "Documents",
            "Keys", "Books", "Bags", "Other"
        );

        // Set max date to today for date picker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // disable future dates and empty cells
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        // Initially hide the remove image button
        removeImageButton.setVisible(false);

        // Set up reward field disable/enable based on item type
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            rewardField.setDisable(newVal == foundRadio);
        });

        // Check if current user is admin 
        net.javaguids.lost_and_found.model.users.User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == net.javaguids.lost_and_found.model.enums.UserRole.ADMIN) {
            isAdminMode = true;
            // Enable admin mode features
            userSelectionBox.setVisible(true);
            userSelectionBox.setManaged(true);
            statusSelectionBox.setVisible(true);
            statusSelectionBox.setManaged(true);
            loadUsers();
            loadStatuses();
        }

        // Check if this is edit mode from context
        Item itemToEdit = EditItemContext.getItem();
        if (itemToEdit != null) {
            setEditMode(itemToEdit, null);
            EditItemContext.clear(); // Clear context after use
        }
    }

    // Loads all regular users into the user selection dropdown for admin mode only
    private void loadUsers() {
        UserRepository userRepository = UserRepository.getInstance();
        java.util.List<net.javaguids.lost_and_found.model.users.User> allUsers = userRepository.getAllUsers();

        javafx.collections.ObservableList<String> usernames = javafx.collections.FXCollections.observableArrayList();

        // Filter out admin and moderator users - only regular users can have items
        for (net.javaguids.lost_and_found.model.users.User user : allUsers) {
            if (user.getRole() != net.javaguids.lost_and_found.model.enums.UserRole.ADMIN &&
                user.getRole() != net.javaguids.lost_and_found.model.enums.UserRole.MODERATOR) {
                usernames.add(user.getUsername());
            }
        }

        userComboBox.setItems(usernames);

        // Select first user by default if available
        if (!usernames.isEmpty()) {
            userComboBox.getSelectionModel().selectFirst();
        }
    }

    // Loads available item statuses into the status selection dropdown for admin mode only.
    private void loadStatuses() {
        javafx.collections.ObservableList<String> statuses = javafx.collections.FXCollections.observableArrayList();

        // Add all possible item statuses
        statuses.add("ACTIVE");
        statuses.add("RESOLVED");

        statusComboBox.setItems(statuses);
        statusComboBox.setValue("ACTIVE"); // Default to ACTIVE
    }

    // Configures the controller for edit mode with an existing item.
    // populates fields with item data and stores original values for change detection.
    public void setEditMode(Item item, UserDashboardController dashboard) {
        this.editingItem = item;
        this.dashboardController = dashboard;

        // Update the title label to indicate edit mode
        if (titleLabel != null) {
            titleLabel.setText("Edit Item");
        }

        // Store original values for change detection
        originalTitle = item.getTitle();
        originalDescription = item.getDescription();
        originalCategory = item.getCategory();
        originalLocation = item.getLocation();
        originalImagePath = item.getImagePath();

        // Populate fields with existing item data
        titleField.setText(item.getTitle());
        descriptionArea.setText(item.getDescription());
        categoryCombo.setValue(item.getCategory());
        locationField.setText(item.getLocation());

        // In admin mode, select the user who owns this item and current status
        if (isAdminMode && userComboBox != null) {
            UserRepository userRepository = UserRepository.getInstance();
            net.javaguids.lost_and_found.model.users.User itemOwner = userRepository.getUserById(item.getPostedByUserId());
            if (itemOwner != null) {
                originalAssignedUser = itemOwner.getUsername();
                userComboBox.setValue(itemOwner.getUsername());
            }

            // Set current item status
            if (statusComboBox != null) {
                originalStatus = item.getStatus().toString();
                statusComboBox.setValue(item.getStatus().toString());
            }
        }

        // Configure fields based on item type (lost vs found)
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            lostRadio.setSelected(true);
            originalType = "LOST";
            originalDate = lostItem.getDateLost().toLocalDate().toString();
            originalReward = String.valueOf(lostItem.getReward());
            datePicker.setValue(lostItem.getDateLost().toLocalDate());
            rewardField.setText(String.valueOf(lostItem.getReward()));
        } else if (item instanceof FoundItem) {
            FoundItem foundItem = (FoundItem) item;
            foundRadio.setSelected(true);
            originalType = "FOUND";
            originalDate = foundItem.getDateFound().toLocalDate().toString();
            datePicker.setValue(foundItem.getDateFound().toLocalDate());
            rewardField.setDisable(true); // no reward for found items
        }

        // laod image if exists
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            this.imagePath = item.getImagePath();
            imagePathLabel.setText("Image: " + new File(item.getImagePath()).getName());
        }
    }

    // Handles GO BACK button click
    @FXML
    public void handleGoBack() {
        // retrieve navigation context for previous page
        String previousPage = NavigationContext.getPreviousPage();
        String previousTitle = NavigationContext.getPreviousTitle();
        NavigationContext.clear();

        // navigate to previous page if available, otherwise go back to dashboard
        if (previousPage != null && previousTitle != null) {
            NavigationManager.navigateTo(previousPage, previousTitle);
        } else {
            NavigationManager.goBack();
        }
    }

    // Handles SUBMIT button click to create or update an item after validation
    @FXML
    public void handleSubmit() {
        // collect form data
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = categoryCombo.getValue();
        String location = locationField.getText().trim();

        // Validate all required fields are filled
        if (title.isEmpty() || description.isEmpty() || category == null || location.isEmpty()) {
            AlertUtil.showAlert("Error", "Please fill in all required fields", Alert.AlertType.ERROR);
            return;
        }

        // Validate minimum length for title (at least 3 characters)
        if (title.length() < 3) {
            AlertUtil.showAlert("Error", "Title must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }

        // Validate minimum length for description (at least 10 characters)
        if (description.length() < 10) {
            AlertUtil.showAlert("Error", "Description must be at least 10 characters long", Alert.AlertType.ERROR);
            return;
        }

        // Validate date is selected
        if (datePicker.getValue() == null) {
            AlertUtil.showAlert("Error", "Please select a date", Alert.AlertType.ERROR);
            return;
        }

        // Validate date is not in the future
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            AlertUtil.showAlert("Error", "Date cannot be in the future", Alert.AlertType.ERROR);
            return;
        }

        // Validate reward is not negative
        if (lostRadio.isSelected() && !rewardField.getText().isEmpty()) {
            try {
                double reward = Double.parseDouble(rewardField.getText());
                if (reward < 0) {
                    AlertUtil.showAlert("Error", "Reward cannot be negative", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtil.showAlert("Error", "Invalid reward amount", Alert.AlertType.ERROR);
                return;
            }
        }

        // Edit Mode Processing
        if (editingItem != null) {
            // Collect current field values for comparison
            String currentType = lostRadio.isSelected() ? "LOST" : "FOUND";
            String currentDate = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
            String currentReward = rewardField.getText().trim();
            String currentImagePath = imagePath;
            String currentAssignedUser = isAdminMode && userComboBox != null ? userComboBox.getValue() : null;
            String currentStatus = isAdminMode && statusComboBox != null ? statusComboBox.getValue() : null;
            // Check if any changes were made
            boolean hasChanges = !title.equals(originalTitle) ||
                                !description.equals(originalDescription) ||
                                !category.equals(originalCategory) ||
                                !location.equals(originalLocation) ||
                                !currentType.equals(originalType) ||
                                !currentDate.equals(originalDate) ||
                                (originalReward != null && !currentReward.equals(originalReward)) ||
                                ((currentImagePath == null && originalImagePath != null) ||
                                 (currentImagePath != null && !currentImagePath.equals(originalImagePath))) ||
                                (isAdminMode && currentAssignedUser != null && !currentAssignedUser.equals(originalAssignedUser)) ||
                                (isAdminMode && currentStatus != null && !currentStatus.equals(originalStatus));

            // exit if no changes detected
            if (!hasChanges) {
                AlertUtil.showAlert("No Changes", "No changes were made to the item", Alert.AlertType.INFORMATION);
                return;
            }

            // Get the user ID 
            String userId = editingItem.getPostedByUserId();

            // In admin mode, allow changing the user assignment
            if (isAdminMode) {
                String selectedUsername = userComboBox.getValue();
                if (selectedUsername == null || selectedUsername.isEmpty()) {
                    AlertUtil.showAlert("Error", "Please select a user for this item", Alert.AlertType.ERROR);
                    return;
                }

                UserRepository userRepository = UserRepository.getInstance();
                net.javaguids.lost_and_found.model.users.User selectedUser = userRepository.getUserByUsername(selectedUsername);
                if (selectedUser == null) {
                    AlertUtil.showAlert("Error", "Selected user not found", Alert.AlertType.ERROR);
                    return;
                }
                userId = selectedUser.getUserId();
            }

            // If user changed, we need to recreate the item
            if (!userId.equals(editingItem.getPostedByUserId())) {
                String itemId = editingItem.getItemId();
                LocalDateTime date = datePicker.getValue().atStartOfDay();

                // Create new item with updated user
                Item newItem;
                if (editingItem instanceof LostItem) {
                    double reward = 0.0;
                    try {
                        if (!rewardField.getText().isEmpty()) {
                            reward = Double.parseDouble(rewardField.getText());
                        }
                    } catch (NumberFormatException e) {
                        AlertUtil.showAlert("Error", "Invalid reward amount", Alert.AlertType.ERROR);
                        return;
                    }
                    newItem = new LostItem(itemId, title, description, category, location, userId, date, reward);
                } else {
                    newItem = new FoundItem(itemId, title, description, category, location, userId, date);
                }

                // Set status - admin can change it, otherwise keep original
                if (isAdminMode && statusComboBox != null && statusComboBox.getValue() != null) {
                    newItem.setStatus(net.javaguids.lost_and_found.model.enums.ItemStatus.valueOf(statusComboBox.getValue()));
                } else {
                    newItem.setStatus(editingItem.getStatus());
                }
                // Set image path
                if (imagePath != null) {
                    newItem.setImagePath(imagePath);
                } else if (editingItem.getImagePath() != null) {
                    newItem.setImagePath(editingItem.getImagePath());
                }
                // Update item via service
                boolean success = itemService.updateItem(newItem);
                if (success) {
                    AlertUtil.showAlert("Success", "Item updated successfully", Alert.AlertType.INFORMATION);
                    if (dashboardController != null) {
                        dashboardController.loadMyItems();
                    }
                    NavigationManager.goBack();
                } else {
                    AlertUtil.showAlert("Error", "Failed to update item", Alert.AlertType.ERROR);
                }
            } else {
                // User not changed, just update the existing item
                editingItem.setTitle(title);
                editingItem.setDescription(description);
                editingItem.setCategory(category);
                editingItem.setLocation(location);

                // Update type-specific fields
                if (editingItem instanceof LostItem) {
                    LostItem lostItem = (LostItem) editingItem;
                    lostItem.setDateLost(datePicker.getValue().atStartOfDay());
                    try {
                        if (!rewardField.getText().isEmpty()) {
                            lostItem.setReward(Double.parseDouble(rewardField.getText()));
                        }
                    } catch (NumberFormatException e) {
                        AlertUtil.showAlert("Error", "Invalid reward amount", Alert.AlertType.ERROR);
                        return;
                    }
                } else if (editingItem instanceof FoundItem) {
                    FoundItem foundItem = (FoundItem) editingItem;
                    foundItem.setDateFound(datePicker.getValue().atStartOfDay());
                }
                // Update image path if changed 
                if (imagePath != null) {
                    editingItem.setImagePath(imagePath);
                }

                // Apply status change if admin changed it
                if (isAdminMode && statusComboBox != null && statusComboBox.getValue() != null) {
                    editingItem.setStatus(net.javaguids.lost_and_found.model.enums.ItemStatus.valueOf(statusComboBox.getValue()));
                }
                // update item via service
                boolean success = itemService.updateItem(editingItem);
                if (success) {
                    AlertUtil.showAlert("Success", "Item updated successfully", Alert.AlertType.INFORMATION);
                    if (dashboardController != null) {
                        dashboardController.loadMyItems();
                    }
                    NavigationManager.goBack();
                } else {
                    AlertUtil.showAlert("Error", "Failed to update item", Alert.AlertType.ERROR);
                }
            }
        } else {
            // create new item mode
            String itemId = UUID.randomUUID().toString();
            String userId;

            // In admin mode, get the selected user's ID
            if (isAdminMode) {
                String selectedUsername = userComboBox.getValue();
                if (selectedUsername == null || selectedUsername.isEmpty()) {
                    AlertUtil.showAlert("Error", "Please select a user for this item", Alert.AlertType.ERROR);
                    return;
                }

                UserRepository userRepository = UserRepository.getInstance();
                net.javaguids.lost_and_found.model.users.User selectedUser = userRepository.getUserByUsername(selectedUsername);
                if (selectedUser == null) {
                    AlertUtil.showAlert("Error", "Selected user not found", Alert.AlertType.ERROR);
                    return;
                }
                userId = selectedUser.getUserId();
            } else {
                // Regular user creates item for themselves
                userId = AuthService.getCurrentUser().getUserId();
            }

            LocalDateTime date = datePicker.getValue().atStartOfDay();

            // Create appropriate item type based on button selection
            Item item;
            if (lostRadio.isSelected()) {
                double reward = 0.0;
                try {
                    if (!rewardField.getText().isEmpty()) {
                        reward = Double.parseDouble(rewardField.getText());
                    }
                } catch (NumberFormatException e) {
                    AlertUtil.showAlert("Error", "Invalid reward amount", Alert.AlertType.ERROR);
                    return;
                }
                item = new LostItem(itemId, title, description, category, location, userId, date, reward);
            } else {
                item = new FoundItem(itemId, title, description, category, location, userId, date);
            }

            // Set image path if provided
            if (imagePath != null) {
                item.setImagePath(imagePath);
            }

            // Apply status if admin is creating the item
            if (isAdminMode && statusComboBox != null && statusComboBox.getValue() != null) {
                item.setStatus(net.javaguids.lost_and_found.model.enums.ItemStatus.valueOf(statusComboBox.getValue()));
            }

            // Create item via service
            boolean success = itemService.postItem(item);
            if (success) {
                AlertUtil.showAlert("Success", "Item posted successfully", Alert.AlertType.INFORMATION);
                NavigationManager.goBack();
            } else {
                AlertUtil.showAlert("Error", "Failed to post item", Alert.AlertType.ERROR);
            }
        }
    }

    // Handles UPLOAD IMAGE button click to select and save an image file
    @FXML
    public void handleUploadImage() {
        // Configure file chooser for image selection
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Show file chooser dialog
        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        // Process selected file
        if (file != null) {
            String itemId = UUID.randomUUID().toString();
            //TODO: Uncomment when FileHandler is implemented
            //imagePath = FileHandler.saveImage(file, itemId);
            //if (imagePath != null) {
            //    imagePathLabel.setText("Image: " + file.getName());
            //    removeImageButton.setVisible(true);
            }
        }
    }

    // Image removal functionality: clears the selected image and updates UI
    //@FXML
   //public void handleRemoveImage() {
        //imagePath = null;
        //imagePathLabel.setText("No image selected");
        //removeImageButton.setVisible(false);
    //}
//}

