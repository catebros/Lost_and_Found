package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.context.EditUserContext;
import net.javaguids.lost_and_found.utils.ValidationUtil;

import java.util.List;

// Controller for editing an existing user

public class EditUserController {
    // FXML UI components

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> roleCombo;

    private UserRepository userRepository;
    private User userToEdit;
    private String originalUsername;
    private String originalEmail;
    private String originalRole;

    @FXML
    public void initialize() {
        // initialize user repository and role options
        userRepository = UserRepository.getInstance();
        roleCombo.getItems().addAll("USER", "MODERATOR", "ADMIN");

        // Load user from shared context
        User contextUser = EditUserContext.getUser();
        if (contextUser != null) {
            setUser(contextUser);
            // clear the context after loading for memory management
            EditUserContext.clear();
        }
    }

    // set the user to be edited and populate fields with current user data
    public void setUser(User user) {
        this.userToEdit = user;

        // Store original values for change detection and validation
        originalUsername = user.getUsername();
        originalEmail = user.getEmail();
        originalRole = user.getRole().toString();

        // populate fields with current user data
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        roleCombo.setValue(user.getRole().toString());
    }

    // Handle going back to the previous view
    @FXML
    public void handleGoBack() {
        // retrieve previous page info from navigation context
        String previousPage = NavigationContext.getPreviousPage();
        String previousTitle = NavigationContext.getPreviousTitle();
        // clear navigation context after use
        NavigationContext.clear();

        // navigate back to previous page 
        if (previousPage != null && previousTitle != null) {
            NavigationManager.navigateTo(previousPage, previousTitle);
        } else {
            NavigationManager.goBack();
        }
    }

    // Handle updating the user with new values
    @FXML
    public void handleUpdate() {
        if (userToEdit == null) {
            // ensure a user is selected for editing
            AlertUtil.showAlert("Error", "No user selected", Alert.AlertType.ERROR);
            return;
        }

        // Get current values from fields
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newRole = roleCombo.getValue();

        // Check if any changes were made
        boolean hasChanges = !newUsername.equals(originalUsername) ||
                            !newEmail.equals(originalEmail) ||
                            !newRole.equals(originalRole);

        if (!hasChanges) {
            AlertUtil.showAlert("No Changes", "No changes were made to the user", Alert.AlertType.INFORMATION);
            return;
        }

        // Validate fields
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            AlertUtil.showAlert("Error", "Username and email cannot be empty", Alert.AlertType.ERROR);
            return;
        }

        // Validate username length 
        if (newUsername.length() < 3) {
            AlertUtil.showAlert("Error", "Username must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(newEmail)) {
            AlertUtil.showAlert("Error", "Invalid email format", Alert.AlertType.ERROR);
            return;
        }

        // Check for username uniqueness if username was changed
        if (!newUsername.equals(originalUsername)) {
            User existingUser = userRepository.getUserByUsername(newUsername);
            if (existingUser != null) {
                AlertUtil.showAlert("Error", "Username already exists", Alert.AlertType.ERROR);
                return;
            }
        }

        // Check for email uniqueness if email was changed
        if (!newEmail.equals(originalEmail)) {
            User existingEmailUser = userRepository.getUserByEmail(newEmail);
            if (existingEmailUser != null && !existingEmailUser.getUserId().equals(userToEdit.getUserId())) {
                AlertUtil.showAlert("Error", "Email already exists", Alert.AlertType.ERROR);
                return;
            }
        }

        // Create new user with updated values based on selected role
        User updatedUser;
        if (newRole.equals("ADMIN")) {
            updatedUser = new Admin(userToEdit.getUserId(), newUsername, newEmail, userToEdit.getPasswordHash());
        } else if (newRole.equals("MODERATOR")) {
            updatedUser = new Moderator(userToEdit.getUserId(), newUsername, newEmail, userToEdit.getPasswordHash());
        } else {
            // default to RegularUser for "USER" role
            updatedUser = new RegularUser(userToEdit.getUserId(), newUsername, newEmail, userToEdit.getPasswordHash());
        }

        // Attempt to update the user in the db
        boolean success = userRepository.updateUser(updatedUser);
        if (success) {
            AlertUtil.showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
            NavigationManager.goBack();
        } else {
            AlertUtil.showAlert("Error", "Failed to update user", Alert.AlertType.ERROR);
        }
    }
}