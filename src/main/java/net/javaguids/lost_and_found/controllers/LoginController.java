package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // make sure both fields have something in them
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showAlert("Error", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }

        // try to authenticate the user
        User user = AuthService.login(username, password);

        if (user != null) {
            // redirect based on what kind of user it is
            if (user.getRole() == UserRole.ADMIN) {
                NavigationManager.navigateTo("admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard");
            } else if (user.getRole() == UserRole.MODERATOR) {
                NavigationManager.navigateTo("moderator-dashboard-view.fxml", "Lost and Found - Moderator Dashboard");
            } else {
                NavigationManager.navigateTo("user-dashboard-view.fxml", "Lost and Found - Dashboard");
            }
        } else {
            // login didn't work, show error
            AlertUtil.showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRegister() {
        // send them to the registration page
        NavigationManager.navigateTo("register-view.fxml", "Lost and Found - Register");
    }
}