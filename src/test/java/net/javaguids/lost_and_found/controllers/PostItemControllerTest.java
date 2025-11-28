package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.enums.UserRole;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.context.EditItemContext;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PostItemController Tests")
class PostItemControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private PostItemController controller;

    @Mock
    private ItemService mockItemService;

    @Mock
    private User mockCurrentUser;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private UserDashboardController mockDashboardController;

    // Mocked JavaFX components
    @Mock
    private TextField mockTitleField;

    @Mock
    private TextArea mockDescriptionArea;

    @Mock
    private ComboBox<String> mockCategoryCombo;

    @Mock
    private TextField mockLocationField;

    @Mock
    private DatePicker mockDatePicker;

    @Mock
    private RadioButton mockLostRadio;

    @Mock
    private RadioButton mockFoundRadio;

    @Mock
    private TextField mockRewardField;

    @Mock
    private Label mockImagePathLabel;

    @Mock
    private Label mockTitleLabel;

    @Mock
    private Button mockRemoveImageButton;

    @Mock
    private VBox mockUserSelectionBox;

    @Mock
    private ComboBox<String> mockUserComboBox;

    @Mock
    private VBox mockStatusSelectionBox;

    @Mock
    private ComboBox<String> mockStatusComboBox;

    @Mock
    private SingleSelectionModel<String> mockUserSelectionModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PostItemController();
        setupUIComponentMocks();
    }

    /**
     * Helper method to inject mocked UI components
     */
    private void injectField(String fieldName, Object value) {
        try {
            var field = PostItemController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject field: " + fieldName + " - " + e.getMessage());
        }
    }

    /**
     * Helper method to inject private fields using reflection
     */
    private Object getField(String fieldName) {
        try {
            var field = PostItemController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to get field: " + fieldName + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to invoke private methods using reflection
     */
    private void invokePrivateMethod(String methodName) {
        try {
            var method = PostItemController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + methodName + " - " + e.getMessage());
        }
    }

    /**
     * Helper method to setup all mocked UI components
     */
    private void setupUIComponentMocks() {
        // Setup default mock behaviors
        when(mockCategoryCombo.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockUserComboBox.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockStatusComboBox.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockUserSelectionBox.isVisible()).thenReturn(false);
        when(mockStatusSelectionBox.isVisible()).thenReturn(false);
        when(mockRemoveImageButton.isVisible()).thenReturn(false);
        when(mockRewardField.isDisabled()).thenReturn(false);
        when(mockUserComboBox.getSelectionModel()).thenReturn(mockUserSelectionModel);
        when(mockTitleField.getText()).thenReturn("");
        when(mockDescriptionArea.getText()).thenReturn("");
        when(mockLocationField.getText()).thenReturn("");
        when(mockRewardField.getText()).thenReturn("");

        // Inject all mocks into controller
        injectField("titleField", mockTitleField);
        injectField("descriptionArea", mockDescriptionArea);
        injectField("categoryCombo", mockCategoryCombo);
        injectField("locationField", mockLocationField);
        injectField("datePicker", mockDatePicker);
        injectField("lostRadio", mockLostRadio);
        injectField("foundRadio", mockFoundRadio);
        injectField("rewardField", mockRewardField);
        injectField("imagePathLabel", mockImagePathLabel);
        injectField("titleLabel", mockTitleLabel);
        injectField("removeImageButton", mockRemoveImageButton);
        injectField("userSelectionBox", mockUserSelectionBox);
        injectField("userComboBox", mockUserComboBox);
        injectField("statusSelectionBox", mockStatusSelectionBox);
        injectField("statusComboBox", mockStatusComboBox);
        injectField("itemService", mockItemService);
    }

    @Test
    @DisplayName("Test 1: Initialize sets up UI components correctly")
    void testInitializeSetupComponents() {
        // Setup category combo to track items added
        ObservableList<String> categories = FXCollections.observableArrayList();
        when(mockCategoryCombo.getItems()).thenReturn(categories);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<EditItemContext> contextMock = mockStatic(EditItemContext.class)) {
            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            when(mockCurrentUser.getRole()).thenReturn(UserRole.USER);
            contextMock.when(EditItemContext::getItem).thenReturn(null);

            // Act
            controller.initialize();

            // Assert
            verify(mockCategoryCombo).getItems();
            assertTrue(categories.contains("Electronics"), "Electronics category should be added");
            assertTrue(categories.contains("Clothing"), "Clothing category should be added");
            assertEquals(8, categories.size(), "Should have 8 categories");
        }
    }

    @Test
    @DisplayName("Test 2: Initialize enables admin mode UI when current user is admin")
    void testInitializeAdminMode() {
        // Setup mocks
        ObservableList<String> users = FXCollections.observableArrayList();
        ObservableList<String> categories = FXCollections.observableArrayList();

        when(mockCategoryCombo.getItems()).thenReturn(categories);
        when(mockUserComboBox.getItems()).thenReturn(users);

        User adminUser = mock(User.class);
        when(adminUser.getRole()).thenReturn(UserRole.ADMIN);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<EditItemContext> contextMock = mockStatic(EditItemContext.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {
            authMock.when(AuthService::getCurrentUser).thenReturn(adminUser);
            contextMock.when(EditItemContext::getItem).thenReturn(null);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            when(mockUserRepository.getAllUsers()).thenReturn(Collections.emptyList());

            // Act
            controller.initialize();

            // Assert
            ArgumentCaptor<ObservableList<String>> statusesCaptor = ArgumentCaptor.forClass(ObservableList.class);
            verify(mockStatusComboBox).setItems(statusesCaptor.capture());
            ObservableList<String> statuses = statusesCaptor.getValue();
            verify(mockUserSelectionBox).setVisible(true);
            verify(mockStatusSelectionBox).setVisible(true);
            assertTrue(statuses.contains("ACTIVE"), "Status should contain ACTIVE");
            assertTrue(statuses.contains("RESOLVED"), "Status should contain RESOLVED");
        }
    }

    @Test
    @DisplayName("Test 3: LoadUsers filters out admin and moderator users")
    void testLoadUsersFiltering() {
        // Setup mocks
        User regularUser1 = mock(User.class);
        when(regularUser1.getRole()).thenReturn(UserRole.USER);
        when(regularUser1.getUsername()).thenReturn("john_doe");

        User regularUser2 = mock(User.class);
        when(regularUser2.getRole()).thenReturn(UserRole.USER);
        when(regularUser2.getUsername()).thenReturn("jane_smith");

        User adminUser = mock(User.class);
        when(adminUser.getRole()).thenReturn(UserRole.ADMIN);
        when(adminUser.getUsername()).thenReturn("admin_user");

        User moderatorUser = mock(User.class);
        when(moderatorUser.getRole()).thenReturn(UserRole.MODERATOR);
        when(moderatorUser.getUsername()).thenReturn("moderator_user");

        List<User> allUsers = Arrays.asList(regularUser1, regularUser2, adminUser, moderatorUser);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<EditItemContext> contextMock = mockStatic(EditItemContext.class)) {
            authMock.when(AuthService::getCurrentUser).thenReturn(adminUser);
            contextMock.when(EditItemContext::getItem).thenReturn(null);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            when(mockUserRepository.getAllUsers()).thenReturn(allUsers);

            // Act via initialize (admin mode triggers loadUsers)
            controller.initialize();

            // Assert
            ArgumentCaptor<ObservableList<String>> usersCaptor = ArgumentCaptor.forClass(ObservableList.class);
            verify(mockUserComboBox).setItems(usersCaptor.capture());
            ObservableList<String> users = usersCaptor.getValue();
            assertTrue(users.contains("john_doe"), "Regular user should be in list");
            assertTrue(users.contains("jane_smith"), "Regular user should be in list");
            assertFalse(users.contains("admin_user"), "Admin user should be filtered out");
            assertFalse(users.contains("moderator_user"), "Moderator user should be filtered out");
            assertEquals(2, users.size(), "Should have exactly 2 regular users");
        }
    }

    @Test
    @DisplayName("Test 4: LoadStatuses populates status dropdown correctly")
    void testLoadStatuses() {
        // Act
        invokePrivateMethod("loadStatuses");

        // Assert
        ArgumentCaptor<ObservableList<String>> statusesCaptor = ArgumentCaptor.forClass(ObservableList.class);
        verify(mockStatusComboBox).setItems(statusesCaptor.capture());
        ObservableList<String> capturedStatuses = statusesCaptor.getValue();
        assertTrue(capturedStatuses.contains("ACTIVE"), "ACTIVE status should exist");
        assertTrue(capturedStatuses.contains("RESOLVED"), "RESOLVED status should exist");
        assertEquals(2, capturedStatuses.size(), "Should have exactly 2 statuses");
        verify(mockStatusComboBox).setValue("ACTIVE");
    }

    @Test
    @DisplayName("Test 5: SetEditMode populates fields for Lost items correctly")
    void testSetEditModeLostItem() {
        // Arrange
        String lostItemId = UUID.randomUUID().toString();
        LocalDateTime dateLost = LocalDateTime.of(2024, 11, 20, 10, 30);
        LostItem lostItem = new LostItem(
            lostItemId,
            "Lost Wallet",
            "Brown leather wallet with credit cards",
            "Accessories",
            "Downtown Park",
            "user123",
            dateLost,
            50.0
        );
        lostItem.setStatus(ItemStatus.ACTIVE);

        // Act
        controller.setEditMode(lostItem, null);

        // Assert
        verify(mockTitleLabel).setText("Edit Item");
        verify(mockTitleField).setText("Lost Wallet");
        verify(mockDescriptionArea).setText("Brown leather wallet with credit cards");
        verify(mockCategoryCombo).setValue("Accessories");
        verify(mockLocationField).setText("Downtown Park");
        verify(mockDatePicker).setValue(dateLost.toLocalDate());
        verify(mockLostRadio).setSelected(true);
        verify(mockRewardField).setText("50.0");
    }

    @Test
    @DisplayName("Test 6: SetEditMode populates fields for Found items correctly")
    void testSetEditModeFoundItem() {
        // Arrange
        String foundItemId = UUID.randomUUID().toString();
        LocalDateTime dateFound = LocalDateTime.of(2024, 11, 22, 14, 45);
        FoundItem foundItem = new FoundItem(
            foundItemId,
            "Found Phone",
            "Black smartphone found at bus station",
            "Electronics",
            "Bus Station",
            "user456",
            dateFound
        );
        foundItem.setStatus(ItemStatus.ACTIVE);

        // Act
        controller.setEditMode(foundItem, mockDashboardController);

        // Assert
        verify(mockTitleLabel).setText("Edit Item");
        verify(mockTitleField).setText("Found Phone");
        verify(mockDescriptionArea).setText("Black smartphone found at bus station");
        verify(mockCategoryCombo).setValue("Electronics");
        verify(mockLocationField).setText("Bus Station");
        verify(mockDatePicker).setValue(dateFound.toLocalDate());
        verify(mockFoundRadio).setSelected(true);
        verify(mockRewardField).setDisable(true);
    }

    @Test
    @DisplayName("Test 7: HandleSubmit rejects empty required fields")
    void testHandleSubmitEmptyFields() {
        // Arrange
        when(mockTitleField.getText()).thenReturn("");
        when(mockDescriptionArea.getText()).thenReturn("Description");
        when(mockCategoryCombo.getValue()).thenReturn("Electronics");
        when(mockLocationField.getText()).thenReturn("Location");

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            // Act
            controller.handleSubmit();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Please fill in all required fields", Alert.AlertType.ERROR));
        }
    }

    @Test
    @DisplayName("Test 8: HandleSubmit rejects title shorter than 3 characters")
    void testHandleSubmitShortTitle() {
        // Arrange
        when(mockTitleField.getText()).thenReturn("AB");
        when(mockDescriptionArea.getText()).thenReturn("Valid description");
        when(mockCategoryCombo.getValue()).thenReturn("Electronics");
        when(mockLocationField.getText()).thenReturn("Valid location");

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            // Act
            controller.handleSubmit();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Title must be at least 3 characters long", Alert.AlertType.ERROR));
        }
    }

    @Test
    @DisplayName("Test 9: HandleSubmit rejects description shorter than 10 characters")
    void testHandleSubmitShortDescription() {
        // Arrange
        when(mockTitleField.getText()).thenReturn("Valid Title");
        when(mockDescriptionArea.getText()).thenReturn("Short");
        when(mockCategoryCombo.getValue()).thenReturn("Electronics");
        when(mockLocationField.getText()).thenReturn("Valid location");

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            // Act
            controller.handleSubmit();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Description must be at least 10 characters long", Alert.AlertType.ERROR));
        }
    }

    @Test
    @DisplayName("Test 10: HandleSubmit successfully creates new lost item")
    void testHandleSubmitCreateLostItem() {
        // Arrange
        when(mockTitleField.getText()).thenReturn("Lost Keys");
        when(mockDescriptionArea.getText()).thenReturn("Lost my house keys near the mall");
        when(mockCategoryCombo.getValue()).thenReturn("Keys");
        when(mockLocationField.getText()).thenReturn("Shopping Mall");
        when(mockDatePicker.getValue()).thenReturn(LocalDate.now().minusDays(1));
        when(mockLostRadio.isSelected()).thenReturn(true);
        when(mockFoundRadio.isSelected()).thenReturn(false);
        when(mockRewardField.getText()).thenReturn("25.0");

        when(mockItemService.postItem(any(Item.class))).thenReturn(true);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            when(mockCurrentUser.getUserId()).thenReturn("user789");

            // Act
            controller.handleSubmit();

            // Assert
            verify(mockItemService).postItem(any(LostItem.class));
            alertMock.verify(() -> AlertUtil.showAlert("Success", "Item posted successfully", Alert.AlertType.INFORMATION));
            navMock.verify(NavigationManager::goBack);
        }
    }
}
