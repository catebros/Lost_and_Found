package net.javaguids.lost_and_found.controllers;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.enums.UserRole;
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

class LoginControllerTest extends ApplicationTest {

    private LoginController controller;
    private TextField usernameField;
    private PasswordField passwordField;

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
        controller = new LoginController();
        usernameField = new TextField();
        passwordField = new PasswordField();

        try {
            var usernameFieldRef = LoginController.class.getDeclaredField("usernameField");
            usernameFieldRef.setAccessible(true);
            usernameFieldRef.set(controller, usernameField);

            var passwordFieldRef = LoginController.class.getDeclaredField("passwordField");
            passwordFieldRef.setAccessible(true);
            passwordFieldRef.set(controller, passwordField);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test fields", e);
        }
    }

    @Test
    @DisplayName("Should show error when username is empty")
    void testHandleLogin_EmptyUsername() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            passwordField.setText("password123");

            controller.handleLogin();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("username and password"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when password is empty")
    void testHandleLogin_EmptyPassword() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            passwordField.setText("");

            controller.handleLogin();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("username and password"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when both fields are empty")
    void testHandleLogin_BothFieldsEmpty() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            passwordField.setText("");

            controller.handleLogin();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("username and password"), any()));
        }
    }

    @Test
    @DisplayName("Should show error when credentials are invalid")
    void testHandleLogin_InvalidCredentials() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            usernameField.setText("wronguser");
            passwordField.setText("wrongpass");

            authService.when(() -> AuthService.login("wronguser", "wrongpass"))
                .thenReturn(null);

            controller.handleLogin();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Login Failed"),
                contains("Invalid username or password"), any()));
        }
    }

    @Test
    @DisplayName("Should navigate to user dashboard on successful regular user login")
    void testHandleLogin_SuccessfulLoginAsRegularUser() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("regularuser");
            passwordField.setText("password123");

            User mockUser = mock(User.class);
            when(mockUser.getRole()).thenReturn(UserRole.USER);
            authService.when(() -> AuthService.login("regularuser", "password123"))
                .thenReturn(mockUser);

            controller.handleLogin();

            navManager.verify(() -> NavigationManager.navigateTo(
                "user-dashboard-view.fxml", "Lost and Found - Dashboard"));
        }
    }

    @Test
    @DisplayName("Should navigate to admin dashboard on successful admin login")
    void testHandleLogin_SuccessfulLoginAsAdmin() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("admin");
            passwordField.setText("adminpass");

            User mockUser = mock(User.class);
            when(mockUser.getRole()).thenReturn(UserRole.ADMIN);
            authService.when(() -> AuthService.login("admin", "adminpass"))
                .thenReturn(mockUser);

            controller.handleLogin();

            navManager.verify(() -> NavigationManager.navigateTo(
                "admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard"));
        }
    }

    @Test
    @DisplayName("Should navigate to moderator dashboard on successful moderator login")
    void testHandleLogin_SuccessfulLoginAsModerator() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("moderator");
            passwordField.setText("modpass");

            User mockUser = mock(User.class);
            when(mockUser.getRole()).thenReturn(UserRole.MODERATOR);
            authService.when(() -> AuthService.login("moderator", "modpass"))
                .thenReturn(mockUser);

            controller.handleLogin();

            navManager.verify(() -> NavigationManager.navigateTo(
                "moderator-dashboard-view.fxml", "Lost and Found - Moderator Dashboard"));
        }
    }

    @Test
    @DisplayName("Should navigate to register view when register button is clicked")
    void testHandleRegister() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {
            controller.handleRegister();

            navManager.verify(() -> NavigationManager.navigateTo(
                "register-view.fxml", "Lost and Found - Register"));
        }
    }
}
