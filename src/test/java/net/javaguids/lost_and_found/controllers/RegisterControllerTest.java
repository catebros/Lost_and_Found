package net.javaguids.lost_and_found.controllers;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import net.javaguids.lost_and_found.exceptions.AuthException;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.ApplicationTest;
import static org.mockito.Mockito.*;

class RegisterControllerTest extends ApplicationTest {

    private RegisterController controller;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    @BeforeAll
    static void initToolkit() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    @BeforeEach
    void setUp() {
        controller = new RegisterController();
        usernameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();

        try {
            var usernameFieldRef = RegisterController.class.getDeclaredField("usernameField");
            usernameFieldRef.setAccessible(true);
            usernameFieldRef.set(controller, usernameField);

            var emailFieldRef = RegisterController.class.getDeclaredField("emailField");
            emailFieldRef.setAccessible(true);
            emailFieldRef.set(controller, emailField);

            var passwordFieldRef = RegisterController.class.getDeclaredField("passwordField");
            passwordFieldRef.setAccessible(true);
            passwordFieldRef.set(controller, passwordField);

            var confirmPasswordFieldRef = RegisterController.class.getDeclaredField("confirmPasswordField");
            confirmPasswordFieldRef.setAccessible(true);
            confirmPasswordFieldRef.set(controller, confirmPasswordField);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test fields", e);
        }
    }

    @Test
    @DisplayName("Should show error when all fields are empty")
    void testHandleRegister_AllFieldsEmpty() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Please fill in all fields"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when username is missing")
    void testHandleRegister_MissingUsername() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            emailField.setText("test@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Please fill in all fields"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when email is missing")
    void testHandleRegister_MissingEmail() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Please fill in all fields"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when password is missing")
    void testHandleRegister_MissingPassword() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            passwordField.setText("");
            confirmPasswordField.setText("password123");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Please fill in all fields"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when confirm password is missing")
    void testHandleRegister_MissingConfirmPassword() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Please fill in all fields"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when passwords do not match")
    void testHandleRegister_PasswordsDoNotMatch() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("differentpassword");

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                eq("Passwords do not match"), any()));
        }
    }

    @Test
    @DisplayName("Should register successfully and navigate to login")
    void testHandleRegister_SuccessfulRegistration() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("newuser");
            emailField.setText("newuser@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            User mockUser = mock(User.class);
            authService.when(() -> AuthService.register("newuser", "newuser@example.com", "password123"))
                .thenReturn(mockUser);

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("Registration successful"), any()));

            navManager.verify(() -> NavigationManager.navigateTo(
                "login-view.fxml", "Lost and Found - Login"));
        }
    }

    @Test
    @DisplayName("Should show error when username already exists")
    void testHandleRegister_RegistrationFailsDueToExistingUsername() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            usernameField.setText("existinguser");
            emailField.setText("test@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            authService.when(() -> AuthService.register("existinguser", "test@example.com", "password123"))
                .thenThrow(new AuthException("Username already exists"));

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Registration Failed"),
                eq("Username already exists"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when email already exists")
    void testHandleRegister_RegistrationFailsDueToExistingEmail() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            usernameField.setText("newuser");
            emailField.setText("existing@example.com");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            authService.when(() -> AuthService.register("newuser", "existing@example.com", "password123"))
                .thenThrow(new AuthException("Email already exists"));

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Registration Failed"),
                eq("Email already exists"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when email format is invalid")
    void testHandleRegister_RegistrationFailsDueToInvalidEmail() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            usernameField.setText("newuser");
            emailField.setText("notanemail");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");

            authService.when(() -> AuthService.register("newuser", "notanemail", "password123"))
                .thenThrow(new AuthException("Invalid email format"));

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Registration Failed"),
                eq("Invalid email format"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when password is too weak")
    void testHandleRegister_RegistrationFailsDueToWeakPassword() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            usernameField.setText("newuser");
            emailField.setText("test@example.com");
            passwordField.setText("weak");
            confirmPasswordField.setText("weak");

            authService.when(() -> AuthService.register("newuser", "test@example.com", "weak"))
                .thenThrow(new AuthException("Password must be at least 8 characters"));

            controller.handleRegister();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Registration Failed"),
                eq("Password must be at least 8 characters"), any()));
        }
    }

    @Test
    @DisplayName("Should navigate to login view when back button is clicked")
    void testHandleBackToLogin() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {
            controller.handleBackToLogin();

            navManager.verify(() -> NavigationManager.navigateTo(
                "login-view.fxml", "Lost and Found - Login"));
        }
    }
}
