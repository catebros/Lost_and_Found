package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import net.javaguids.lost_and_found.analytics.ActivityLog;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.users.Admin;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminDashboardController Tests")
class AdminDashboardControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private AdminDashboardController controller;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private MessageRepository mockMessageRepository;

    @Mock
    private TableView<User> mockUsersTable;

    @Mock
    private TableView<Item> mockItemsTable;

    @Mock
    private TextArea mockLogsArea;

    @Mock
    private Label mockTotalUsersLabel;

    @Mock
    private Label mockTotalItemsLabel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AdminDashboardController();

        // Inject mocks using reflection
        try {
            var userRepoField = AdminDashboardController.class.getDeclaredField("userRepository");
            userRepoField.setAccessible(true);
            userRepoField.set(controller, mockUserRepository);

            var itemRepoField = AdminDashboardController.class.getDeclaredField("itemRepository");
            itemRepoField.setAccessible(true);
            itemRepoField.set(controller, mockItemRepository);

            var messageRepoField = AdminDashboardController.class.getDeclaredField("messageRepository");
            messageRepoField.setAccessible(true);
            messageRepoField.set(controller, mockMessageRepository);

            var usersTableField = AdminDashboardController.class.getDeclaredField("usersTable");
            usersTableField.setAccessible(true);
            usersTableField.set(controller, mockUsersTable);

            var itemsTableField = AdminDashboardController.class.getDeclaredField("itemsTable");
            itemsTableField.setAccessible(true);
            itemsTableField.set(controller, mockItemsTable);

            var logsAreaField = AdminDashboardController.class.getDeclaredField("logsArea");
            logsAreaField.setAccessible(true);
            logsAreaField.set(controller, mockLogsArea);

            var totalUsersLabelField = AdminDashboardController.class.getDeclaredField("totalUsersLabel");
            totalUsersLabelField.setAccessible(true);
            totalUsersLabelField.set(controller, mockTotalUsersLabel);

            var totalItemsLabelField = AdminDashboardController.class.getDeclaredField("totalItemsLabel");
            totalItemsLabelField.setAccessible(true);
            totalItemsLabelField.set(controller, mockTotalItemsLabel);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }

        // Setup default mock behaviors
        when(mockUserRepository.getAllUsers()).thenReturn(Collections.emptyList());
        when(mockItemRepository.searchItems(null)).thenReturn(Collections.emptyList());
        when(mockMessageRepository.getActivityLogs(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Should navigate to create user view with correct navigation context")
    void testHandleCreateUser() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            controller.handleCreateUser();

            navContext.verify(() -> NavigationContext.setPreviousPage(
                "admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard"));
            navManager.verify(() -> NavigationManager.navigateTo(
                "create-user-view.fxml", "Create New User"));
        }
    }

    @Test
    @DisplayName("Should navigate to create item view when users exist")
    void testHandleCreateItemWithUsers() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            User mockUser = mock(User.class);
            when(mockUserRepository.getAllUsers()).thenReturn(Arrays.asList(mockUser));

            controller.handleCreateItem();

            navContext.verify(() -> NavigationContext.setPreviousPage(
                "admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard"));
            navManager.verify(() -> NavigationManager.navigateTo(
                "post-item-view.fxml", "Create New Item"));
        }
    }

    @Test
    @DisplayName("Should show warning when trying to create item with no users")
    void testHandleCreateItemWithoutUsers() {
        try (MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {
            when(mockUserRepository.getAllUsers()).thenReturn(Collections.emptyList());

            controller.handleCreateItem();

            alertUtil.verify(() -> AlertUtil.showAlert(eq("No Users Available"),
                contains("must create at least one user"), eq(Alert.AlertType.WARNING)));
        }
    }

    @Test
    @DisplayName("Should navigate back when handleGoBack is called")
    void testHandleGoBack() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {
            controller.handleGoBack();

            navManager.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Should logout and navigate to login view")
    void testHandleLogout() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class)) {

            controller.handleLogout();

            authService.verify(AuthService::logout);
            navManager.verify(() -> NavigationManager.navigateTo(
                "login-view.fxml", "Lost and Found - Login"));
        }
    }

    @Test
    @DisplayName("Should show error when trying to delete own account")
    void testHandleDeleteUserOwnAccount() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            User currentUser = mock(User.class);
            when(currentUser.getUserId()).thenReturn("user1");
            when(currentUser.getUsername()).thenReturn("admin");
            authService.when(AuthService::getCurrentUser).thenReturn(currentUser);

            User userToDelete = mock(User.class);
            when(userToDelete.getUserId()).thenReturn("user1");
            when(userToDelete.getUsername()).thenReturn("admin");

            // Use reflection to call private method
            try {
                var method = AdminDashboardController.class.getDeclaredMethod("handleDeleteUser", User.class);
                method.setAccessible(true);
                method.invoke(controller, userToDelete);
            } catch (Exception e) {
                fail("Failed to invoke handleDeleteUser: " + e.getMessage());
            }

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("cannot delete your own account"), eq(Alert.AlertType.ERROR)));
        }
    }

    @Test
    @DisplayName("Should not process delete when user is null")
    void testHandleDeleteUserNull() {
        try {
            var method = AdminDashboardController.class.getDeclaredMethod("handleDeleteUser", User.class);
            method.setAccessible(true);
            method.invoke(controller, (Object) null);
            // Should complete without errors
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed to invoke handleDeleteUser: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should not process delete when item is null")
    void testHandleDeleteItemNull() {
        try {
            var method = AdminDashboardController.class.getDeclaredMethod("handleDeleteItem", Item.class);
            method.setAccessible(true);
            method.invoke(controller, (Object) null);
            // Should complete without errors
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed to invoke handleDeleteItem: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should load users into table")
    void testLoadUsers() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        List<User> users = Arrays.asList(user1, user2);

        when(mockUserRepository.getAllUsers()).thenReturn(users);

        try {
            var method = AdminDashboardController.class.getDeclaredMethod("loadUsers");
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            fail("Failed to invoke loadUsers: " + e.getMessage());
        }

        verify(mockUsersTable).setItems(any(ObservableList.class));
    }

    @Test
    @DisplayName("Should handle loadItems without errors")
    void testLoadItems() {
        assertDoesNotThrow(() -> {
            var method = AdminDashboardController.class.getDeclaredMethod("loadItems");
            method.setAccessible(true);
            method.invoke(controller);
        });

        verify(mockItemsTable, atMostOnce()).setItems(any(ObservableList.class));
    }

    @Test
    @DisplayName("Should load activity logs and display them formatted")
    void testLoadLogs() {
        ActivityLog log1 = new ActivityLog("log1", "user1", "LOGIN", "User logged in");
        log1.setTimestamp(LocalDateTime.now());
        ActivityLog log2 = new ActivityLog("log2", "user2", "POST_ITEM", "Posted lost item");
        log2.setTimestamp(LocalDateTime.now());
        List<ActivityLog> logs = Arrays.asList(log1, log2);

        User user1 = mock(User.class);
        when(user1.getUsername()).thenReturn("testuser");
        when(mockUserRepository.getUserById("user1")).thenReturn(user1);

        User user2 = mock(User.class);
        when(user2.getUsername()).thenReturn("otheruser");
        when(mockUserRepository.getUserById("user2")).thenReturn(user2);

        when(mockMessageRepository.getActivityLogs(any(), any())).thenReturn(logs);

        try {
            var method = AdminDashboardController.class.getDeclaredMethod("loadLogs");
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            fail("Failed to invoke loadLogs: " + e.getMessage());
        }

        verify(mockLogsArea).setText(contains("Activity Logs"));
    }

    @Test
    @DisplayName("Should handle edit user navigation")
    void testHandleEditUser() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            User userToEdit = mock(User.class);
            when(userToEdit.getUserId()).thenReturn("user1");

            try {
                var method = AdminDashboardController.class.getDeclaredMethod("handleEditUser", User.class);
                method.setAccessible(true);
                method.invoke(controller, userToEdit);
            } catch (Exception e) {
                fail("Failed to invoke handleEditUser: " + e.getMessage());
            }

            navContext.verify(() -> NavigationContext.setPreviousPage(
                "admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard"));
            navManager.verify(() -> NavigationManager.navigateTo(
                "edit-user-view.fxml", "Edit User"));
        }
    }

    @Test
    @DisplayName("Should handle edit item navigation")
    void testHandleEditItem() {
        try (MockedStatic<NavigationManager> navManager = mockStatic(NavigationManager.class);
             MockedStatic<NavigationContext> navContext = mockStatic(NavigationContext.class)) {

            Item itemToEdit = mock(Item.class);
            when(itemToEdit.getItemId()).thenReturn("item1");

            try {
                var method = AdminDashboardController.class.getDeclaredMethod("handleEditItem", Item.class);
                method.setAccessible(true);
                method.invoke(controller, itemToEdit);
            } catch (Exception e) {
                fail("Failed to invoke handleEditItem: " + e.getMessage());
            }

            navContext.verify(() -> NavigationContext.setPreviousPage(
                "admin-dashboard-view.fxml", "Lost and Found - Admin Dashboard"));
            navManager.verify(() -> NavigationManager.navigateTo(
                "post-item-view.fxml", "Edit Item"));
        }
    }

    @Test
    @DisplayName("Should not edit null user")
    void testHandleEditUserNull() {
        try {
            var method = AdminDashboardController.class.getDeclaredMethod("handleEditUser", User.class);
            method.setAccessible(true);
            method.invoke(controller, (Object) null);
            // Should complete without errors
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed to invoke handleEditUser: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should not edit null item")
    void testHandleEditItemNull() {
        try {
            var method = AdminDashboardController.class.getDeclaredMethod("handleEditItem", Item.class);
            method.setAccessible(true);
            method.invoke(controller, (Object) null);
            // Should complete without errors
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed to invoke handleEditItem: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should prevent editing own user role")
    void testHandleEditUserOwnRole() {
        try (MockedStatic<AuthService> authService = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertUtil = mockStatic(AlertUtil.class)) {

            User currentUser = mock(User.class);
            when(currentUser.getUserId()).thenReturn("user1");
            authService.when(AuthService::getCurrentUser).thenReturn(currentUser);

            User userToEdit = mock(User.class);
            when(userToEdit.getUserId()).thenReturn("user1");

            try {
                var method = AdminDashboardController.class.getDeclaredMethod("handleEditUser", User.class);
                method.setAccessible(true);
                method.invoke(controller, userToEdit);
            } catch (Exception e) {
                fail("Failed to invoke handleEditUser: " + e.getMessage());
            }

            alertUtil.verify(() -> AlertUtil.showAlert(eq("Error"),
                contains("cannot change your own role"), eq(Alert.AlertType.ERROR)));
        }
    }
}
