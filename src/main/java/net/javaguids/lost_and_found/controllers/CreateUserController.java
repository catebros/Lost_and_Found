package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.utils.PasswordUtil;
import net.javaguids.lost_and_found.utils.ValidationUtil;

import java.util.UUID;


// Initialize the controller, set up tables, and load data
// Set up UserRepository and populate the role selection dropdown with available user roles defaulting to "USER".

public class CreateUserController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleCombo;

    private UserRepository userRepository;


    // Initializes the controller 
    // Sets up the UserRepository and populates the role selection dropdown with available user roles, defaulting to "USER".
    @FXML
    public void initialize() {
        // initialize user repository and role options and populate role selection dropdown
        userRepository = UserRepository.getInstance();
        roleCombo.getItems().addAll("USER", "MODERATOR", "ADMIN");
        roleCombo.setValue("USER");
    }

    // Handles the GO BACK button click event
    // Navigates back to previous page using NavigationContext if available, otherwise uses NavigationManager to go back.
    @FXML
    public void handleGoBack() {
        // Retrieve navigation context
        String previousPage = NavigationContext.getPreviousPage();
        String previousTitle = NavigationContext.getPreviousTitle();
        // Clear the navigation context after use
        NavigationContext.clear();

        // Navigate back to the specific previous page if available
        if (previousPage != null && previousTitle != null) {
            NavigationManager.navigateTo(previousPage, previousTitle);
        } else {
            NavigationManager.goBack();
        }
    }


    // Handles the CREATE button click event
    // Validates input fields, creates a new user based on the selected role, and saves it to the UserRepository.
    @FXML
    public void handleCreate() {
        // extract input values
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        // Validate all fields are filled
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertUtil.showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }

        // Validate username length 
        if (username.length() < 3) {
            AlertUtil.showAlert("Error", "Username must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            AlertUtil.showAlert("Error", "Invalid email format", Alert.AlertType.ERROR);
            return;
        }

        // Validate password strength
        if (!ValidationUtil.isValidPassword(password)) {
            AlertUtil.showAlert("Error", "Password must be at least 8 characters long", Alert.AlertType.ERROR);
            return;
        }

        // Check for username uniqueness in db
        if (userRepository.getUserByUsername(username) != null) {
            AlertUtil.showAlert("Error", "Username already exists", Alert.AlertType.ERROR);
            return;
        }

        // Check for email uniqueness in db
        if (userRepository.getUserByEmail(email) != null) {
            AlertUtil.showAlert("Error", "Email already exists", Alert.AlertType.ERROR);
            return;
        }

        // Generate a unique identifier for the new user
        String userId = UUID.randomUUID().toString();
        // Hash the password for secure storage
        String passwordHash = PasswordUtil.hashPassword(password);

        // Create appropriate user object based on selected role 
        User newUser = switch (role) {
            case "ADMIN" -> new Admin(userId, username, email, passwordHash);
            case "MODERATOR" -> new Moderator(userId, username, email, passwordHash);
            default -> new RegularUser(userId, username, email, passwordHash);
        };

         // Attempt to save the new user to the database
        boolean success = userRepository.saveUser(newUser);
        if (success) {
            AlertUtil.showAlert("Success", "User created successfully", Alert.AlertType.INFORMATION);
            NavigationManager.goBack();
        } else {
            AlertUtil.showAlert("Error", "Failed to create user", Alert.AlertType.ERROR);
        }
    }
}