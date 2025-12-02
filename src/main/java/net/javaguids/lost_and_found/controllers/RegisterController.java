package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.javaguids.lost_and_found.exceptions.AuthException;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // check that all fields are filled out
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }

        // make sure the passwords match before continuing
        if (!password.equals(confirmPassword)) {
            AlertUtil.showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        try {
            // attempt to create the new account
            User newUser = AuthService.register(username, email, password);

            // registration worked, let them know
            AlertUtil.showAlert("Success", "Registration successful! You can now login.", Alert.AlertType.INFORMATION);

            // take them back to login so they can sign in
            handleBackToLogin();

        } catch (AuthException e) {
            // something went wrong during registration
            AlertUtil.showAlert("Registration Failed", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleBackToLogin() {
        // navigate back to the login screen
        NavigationManager.navigateTo("login-view.fxml", "Lost and Found - Login");
    }
}
