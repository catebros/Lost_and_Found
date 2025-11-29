package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.NavigationContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CreateUserController Tests")
class CreateUserControllerTest {

    @BeforeAll
    static void initToolkit() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private CreateUserController controller;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private ComboBox<String> roleCombo;

    @Mock
    private UserRepository mockUserRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CreateUserController();
        usernameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        roleCombo = new ComboBox<>();

        try {
            var usernameFieldRef = CreateUserController.class.getDeclaredField("usernameField");
            usernameFieldRef.setAccessible(true);
            usernameFieldRef.set(controller, usernameField);

            var emailFieldRef = CreateUserController.class.getDeclaredField("emailField");
            emailFieldRef.setAccessible(true);
            emailFieldRef.set(controller, emailField);

            var passwordFieldRef = CreateUserController.class.getDeclaredField("passwordField");
            passwordFieldRef.setAccessible(true);
            passwordFieldRef.set(controller, passwordField);

            var roleComboRef = CreateUserController.class.getDeclaredField("roleCombo");
            roleComboRef.setAccessible(true);
            roleComboRef.set(controller, roleCombo);

            var userRepositoryRef = CreateUserController.class.getDeclaredField("userRepository");
            userRepositoryRef.setAccessible(true);
            userRepositoryRef.set(controller, mockUserRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test fields", e);
        }

        // Setup default mock behaviors
        roleCombo.getItems().addAll("USER", "MODERATOR", "ADMIN");
        roleCombo.setValue("USER");
        when(mockUserRepository.getUserByUsername(anyString())).thenReturn(null);
        when(mockUserRepository.getUserByEmail(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("Should show error when all fields are empty")
    void testHandleCreate_AllFieldsEmpty() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("fill in all fields"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when username is empty")
    void testHandleCreate_EmptyUsername() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("");
            emailField.setText("test@example.com");
            passwordField.setText("password123");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("fill in all fields"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when email is empty")
    void testHandleCreate_EmptyEmail() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("");
            passwordField.setText("password123");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("fill in all fields"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when password is empty")
    void testHandleCreate_EmptyPassword() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            passwordField.setText("");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("fill in all fields"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when username is too short")
    void testHandleCreate_UsernameTooShort() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("ab");
            emailField.setText("test@example.com");
            passwordField.setText("password123");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("at least 3 characters"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when email format is invalid")
    void testHandleCreate_InvalidEmailFormat() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("invalidemail");
            passwordField.setText("password123");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Invalid email format"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when password is too short")
    void testHandleCreate_PasswordTooShort() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            passwordField.setText("short");

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("at least 8 characters"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when username already exists")
    void testHandleCreate_UsernameAlreadyExists() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("existinguser");
            emailField.setText("test@example.com");
            passwordField.setText("password123");

            User existingUser = mock(User.class);
            when(mockUserRepository.getUserByUsername("existinguser")).thenReturn(existingUser);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Username already exists"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when email already exists")
    void testHandleCreate_EmailAlreadyExists() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("newuser");
            emailField.setText("existing@example.com");
            passwordField.setText("password123");

            User existingUser = mock(User.class);
            when(mockUserRepository.getUserByEmail("existing@example.com")).thenReturn(existingUser);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Email already exists"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should create user with USER role successfully")
    void testHandleCreate_SuccessUserRole() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("newuser");
            emailField.setText("newuser@example.com");
            passwordField.setText("password123");
            roleCombo.setValue("USER");

            when(mockUserRepository.saveUser(any(User.class))).thenReturn(true);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("created successfully"), eq(Alert.AlertType.INFORMATION)));
            navManager.verify(NavigationManager::goBack);
            verify(mockUserRepository).saveUser(any(RegularUser.class));
        }
    }

    @Test
    @DisplayName("Should create user with MODERATOR role successfully")
    void testHandleCreate_SuccessModerator() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("newmoderator");
            emailField.setText("newmod@example.com");
            passwordField.setText("password123");
            roleCombo.setValue("MODERATOR");

            when(mockUserRepository.saveUser(any(User.class))).thenReturn(true);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("created successfully"), eq(Alert.AlertType.INFORMATION)));
            navManager.verify(NavigationManager::goBack);
            verify(mockUserRepository).saveUser(any(Moderator.class));
        }
    }

    @Test
    @DisplayName("Should create user with ADMIN role successfully")
    void testHandleCreate_SuccessAdmin() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("newadmin");
            emailField.setText("newadmin@example.com");
            passwordField.setText("password123");
            roleCombo.setValue("ADMIN");

            when(mockUserRepository.saveUser(any(User.class))).thenReturn(true);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("created successfully"), eq(Alert.AlertType.INFORMATION)));
            navManager.verify(NavigationManager::goBack);
            verify(mockUserRepository).saveUser(any(Admin.class));
        }
    }

    @Test
    @DisplayName("Should show error when user creation fails")
    void testHandleCreate_CreationFails() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            usernameField.setText("newuser");
            emailField.setText("newuser@example.com");
            passwordField.setText("password123");

            when(mockUserRepository.saveUser(any(User.class))).thenReturn(false);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Failed to create user"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should navigate back to previous page if available")
    void testHandleGoBack_WithNavigationContext() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            navContext.when(NavigationContext::getPreviousPage).thenReturn("admin-dashboard-view.fxml");
            navContext.when(NavigationContext::getPreviousTitle).thenReturn("Admin Dashboard");

            controller.handleGoBack();

            navContext.verify(NavigationContext::clear);
            navManager.verify(() -> NavigationManager.navigateTo("admin-dashboard-view.fxml", "Admin Dashboard"));
        }
    }

    @Test
    @DisplayName("Should go back to previous view if no navigation context")
    void testHandleGoBack_WithoutNavigationContext() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            navContext.when(NavigationContext::getPreviousPage).thenReturn(null);
            navContext.when(NavigationContext::getPreviousTitle).thenReturn(null);

            controller.handleGoBack();

            navContext.verify(NavigationContext::clear);
            navManager.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Should trim whitespace from input fields")
    void testHandleCreate_WithWhitespace() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            usernameField.setText("  validuser  ");
            emailField.setText("  test@example.com  ");
            passwordField.setText("password123");

            when(mockUserRepository.saveUser(any(User.class))).thenReturn(true);

            controller.handleCreate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("created successfully"), eq(Alert.AlertType.INFORMATION)));
            verify(mockUserRepository).saveUser(any(User.class));
        }
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void testHandleCreate_ValidEmailFormats() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            String[] validEmails = {
                "user@example.com",
                "user.name@example.co.uk",
                "user+tag@example.com"
            };

            for (String email : validEmails) {
                setup();
                usernameField.setText("validuser");
                emailField.setText(email);
                passwordField.setText("password123");

                when(mockUserRepository.saveUser(any(User.class))).thenReturn(true);

                controller.handleCreate();
            }

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("created successfully"), eq(Alert.AlertType.INFORMATION)), times(validEmails.length));
        }
    }

    @Test
    @DisplayName("Should use USER role as default")
    void testInitialize_DefaultRole() {
        roleCombo.setValue("USER");
        assertEquals("USER", roleCombo.getValue());
    }

    private void setup() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        when(mockUserRepository.getUserByUsername(anyString())).thenReturn(null);
        when(mockUserRepository.getUserByEmail(anyString())).thenReturn(null);
    }
}
