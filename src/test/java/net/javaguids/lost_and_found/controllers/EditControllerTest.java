package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.Moderator;
import net.javaguids.lost_and_found.model.users.RegularUser;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.context.EditUserContext;
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

@DisplayName("EditUserController Tests")
class EditControllerTest {

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

    private EditUserController controller;
    private TextField usernameField;
    private TextField emailField;
    private ComboBox<String> roleCombo;

    @Mock
    private UserRepository mockUserRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EditUserController();
        usernameField = new TextField();
        emailField = new TextField();
        roleCombo = new ComboBox<>();

        try {
            var usernameFieldRef = EditUserController.class.getDeclaredField("usernameField");
            usernameFieldRef.setAccessible(true);
            usernameFieldRef.set(controller, usernameField);

            var emailFieldRef = EditUserController.class.getDeclaredField("emailField");
            emailFieldRef.setAccessible(true);
            emailFieldRef.set(controller, emailField);

            var roleComboRef = EditUserController.class.getDeclaredField("roleCombo");
            roleComboRef.setAccessible(true);
            roleComboRef.set(controller, roleCombo);

            var userRepositoryRef = EditUserController.class.getDeclaredField("userRepository");
            userRepositoryRef.setAccessible(true);
            userRepositoryRef.set(controller, mockUserRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test fields", e);
        }

        roleCombo.getItems().addAll("USER", "MODERATOR", "ADMIN");
        when(mockUserRepository.getUserByUsername(anyString())).thenReturn(null);
        when(mockUserRepository.getUserByEmail(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("Should set user and populate fields correctly")
    void testSetUser() {
        User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");

        controller.setUser(user);

        assertEquals("testuser", usernameField.getText());
        assertEquals("test@example.com", emailField.getText());
        assertEquals("USER", roleCombo.getValue());
    }

    @Test
    @DisplayName("Should set admin user and populate role as ADMIN")
    void testSetUserAdmin() {
        User user = new Admin("admin1", "adminuser", "admin@example.com", "hashedpassword");

        controller.setUser(user);

        assertEquals("adminuser", usernameField.getText());
        assertEquals("admin@example.com", emailField.getText());
        assertEquals("ADMIN", roleCombo.getValue());
    }

    @Test
    @DisplayName("Should set moderator user and populate role as MODERATOR")
    void testSetUserModerator() {
        User user = new Moderator("mod1", "moduser", "mod@example.com", "hashedpassword");

        controller.setUser(user);

        assertEquals("moduser", usernameField.getText());
        assertEquals("mod@example.com", emailField.getText());
        assertEquals("MODERATOR", roleCombo.getValue());
    }

    @Test
    @DisplayName("Should show error when no changes are made")
    void testHandleUpdate_NoChanges() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("No Changes"),
                contains("No changes were made"), eq(Alert.AlertType.INFORMATION)));
        }
    }

    @Test
    @DisplayName("Should show error when username is empty")
    void testHandleUpdate_EmptyUsername() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("cannot be empty"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when email is empty")
    void testHandleUpdate_EmptyEmail() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("newusername");
            emailField.setText("");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("cannot be empty"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when username is too short")
    void testHandleUpdate_UsernameTooShort() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("ab");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("at least 3 characters"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when email format is invalid")
    void testHandleUpdate_InvalidEmailFormat() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("newuser");
            emailField.setText("invalidemail");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Invalid email format"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when new username already exists")
    void testHandleUpdate_UsernameAlreadyExists() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            User existingUser = mock(User.class);
            when(mockUserRepository.getUserByUsername("existinguser")).thenReturn(existingUser);

            usernameField.setText("existinguser");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Username already exists"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when new email already exists")
    void testHandleUpdate_EmailAlreadyExists() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            User existingUser = mock(User.class);
            when(existingUser.getUserId()).thenReturn("user2");
            when(mockUserRepository.getUserByEmail("existing@example.com")).thenReturn(existingUser);

            usernameField.setText("testuser");
            emailField.setText("existing@example.com");
            roleCombo.setValue("USER");

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Email already exists"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should allow same email if user is editing their own email")
    void testHandleUpdate_EmailAllowedForSameUser() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            // The user with user1 already has the email, so it should be allowed
            User existingUser = mock(User.class);
            when(existingUser.getUserId()).thenReturn("user1");
            when(mockUserRepository.getUserByEmail("test@example.com")).thenReturn(existingUser);

            usernameField.setText("newusername");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
        }
    }

    @Test
    @DisplayName("Should successfully update user with username change")
    void testHandleUpdate_SuccessUsernameChange() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("newusername");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
            navManager.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Should successfully update user with email change")
    void testHandleUpdate_SuccessEmailChange() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("testuser");
            emailField.setText("newemail@example.com");
            roleCombo.setValue("USER");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
            navManager.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Should successfully update user with role change")
    void testHandleUpdate_SuccessRoleChange() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            roleCombo.setValue("ADMIN");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
            verify(mockUserRepository).updateUser(any(Admin.class));
            navManager.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Should update to moderator role")
    void testHandleUpdate_ChangeToModerator() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("testuser");
            emailField.setText("test@example.com");
            roleCombo.setValue("MODERATOR");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            verify(mockUserRepository).updateUser(any(Moderator.class));
        }
    }

    @Test
    @DisplayName("Should show error when update fails")
    void testHandleUpdate_UpdateFails() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("newusername");
            emailField.setText("test@example.com");
            roleCombo.setValue("USER");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(false);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("Failed to update user"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should show error when no user is selected for editing")
    void testHandleUpdate_NoUserSelected() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            // Don't set any user - userToEdit will be null

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("No user selected"), eq(Alert.AlertType.ERROR)));
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
    void testHandleUpdate_WithWhitespace() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("  newusername  ");
            emailField.setText("  newemail@example.com  ");
            roleCombo.setValue("USER");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
        }
    }

    @Test
    @DisplayName("Should accept valid email formats on update")
    void testHandleUpdate_ValidEmailFormats() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            String[] validEmails = {
                "user@example.com",
                "user.name@example.co.uk",
                "user+tag@example.com"
            };

            for (String email : validEmails) {
                setUp();
                User user = new RegularUser("user1", "testuser", "test@example.com", "hashedpassword");
                controller.setUser(user);

                usernameField.setText("testuser");
                emailField.setText(email);
                roleCombo.setValue("USER");

                when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

                controller.handleUpdate();
            }

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)), times(validEmails.length));
        }
    }

    @Test
    @DisplayName("Should update multiple fields simultaneously")
    void testHandleUpdate_MultipleFields() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            User user = new RegularUser("user1", "oldusername", "old@example.com", "hashedpassword");
            controller.setUser(user);

            usernameField.setText("newusername");
            emailField.setText("new@example.com");
            roleCombo.setValue("MODERATOR");

            when(mockUserRepository.updateUser(any(User.class))).thenReturn(true);

            controller.handleUpdate();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Success"),
                contains("updated successfully"), eq(Alert.AlertType.INFORMATION)));
            verify(mockUserRepository).updateUser(any(Moderator.class));
        }
    }
}
