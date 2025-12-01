package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ClaimItemController Tests")
class ClaimItemControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private ClaimItemController controller;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private MessageRepository mockMessageRepository;

    @Mock
    private User mockCurrentUser;

    @Mock
    private User mockOtherUser;

    @Mock
    private ComboBox<String> mockUsersComboBox;

    @Mock
    private ComboBox<ClaimItemController.ItemDisplayWrapper> mockItemsComboBox;

    @Mock
    private Label mockSelectedItemLabel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ClaimItemController();
        setupUIComponentMocks();
    }

    /**
     * Helper method to inject mocked UI components
     */
    private void injectField(String fieldName, Object value) {
        try {
            var field = ClaimItemController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject field: " + fieldName + " - " + e.getMessage());
        }
    }

    /**
     * Helper method to setup all mocked UI components
     */
    private void setupUIComponentMocks() {
        when(mockUsersComboBox.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockItemsComboBox.getItems()).thenReturn(FXCollections.observableArrayList());

        // Mock SelectionModel for usersComboBox
        @SuppressWarnings("unchecked")
        var usersSelectionModel = mock(javafx.scene.control.SingleSelectionModel.class);
        when(mockUsersComboBox.getSelectionModel()).thenReturn(usersSelectionModel);

        // Mock SelectionModel for itemsComboBox
        @SuppressWarnings("unchecked")
        var itemsSelectionModel = mock(javafx.scene.control.SingleSelectionModel.class);
        when(mockItemsComboBox.getSelectionModel()).thenReturn(itemsSelectionModel);

        injectField("usersComboBox", mockUsersComboBox);
        injectField("itemsComboBox", mockItemsComboBox);
        injectField("selectedItemLabel", mockSelectedItemLabel);
    }

    @Test
    @DisplayName("Test 1: SetItem loads conversation users correctly")
    void testSetItemLoadsConversationUsers() {
        // Arrange
        when(mockCurrentUser.getUserId()).thenReturn("currentUser123");

        Item lostItem = new LostItem(
                UUID.randomUUID().toString(),
                "Lost Phone",
                "Black smartphone",
                "Electronics",
                "Downtown",
                "currentUser123",
                LocalDateTime.now(),
                50.0
        );

        List<String> conversationUserIds = Arrays.asList("user1", "user2");

        when(mockOtherUser.getUsername()).thenReturn("john_doe");
        User otherUser2 = mock(User.class);
        when(otherUser2.getUsername()).thenReturn("jane_smith");

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<ItemRepository> itemRepMock = mockStatic(ItemRepository.class);
             MockedStatic<MessageRepository> msgRepMock = mockStatic(MessageRepository.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            itemRepMock.when(ItemRepository::getInstance).thenReturn(mockItemRepository);
            msgRepMock.when(MessageRepository::getInstance).thenReturn(mockMessageRepository);

            when(mockMessageRepository.getUsersFromConversations("currentUser123")).thenReturn(conversationUserIds);
            when(mockUserRepository.getUserById("user1")).thenReturn(mockOtherUser);
            when(mockUserRepository.getUserById("user2")).thenReturn(otherUser2);

            injectField("currentUser", mockCurrentUser);
            injectField("messageRepository", mockMessageRepository);
            injectField("userRepository", mockUserRepository);

            // Act
            controller.setItem(lostItem, null);

            // Assert - Verify users were loaded
            verify(mockUsersComboBox).setItems(any(ObservableList.class));
        }
    }

    @Test
    @DisplayName("Test 2: HandleClaim shows success message")
    void testHandleClaimMarksItemResolved() {
        // Arrange
        Item lostItem = new LostItem(
                UUID.randomUUID().toString(),
                "Lost Wallet",
                "Brown leather wallet",
                "Accessories",
                "Park",
                "user123",
                LocalDateTime.now(),
                0.0
        );

        Item foundItem = new FoundItem(
                UUID.randomUUID().toString(),
                "Found Wallet",
                "Brown leather wallet found",
                "Accessories",
                "Park",
                "user456",
                LocalDateTime.now()
        );

        when(mockCurrentUser.getUserId()).thenReturn("user123");
        when(mockUsersComboBox.getValue()).thenReturn("user456");
        when(mockItemsComboBox.getValue()).thenReturn(new ClaimItemController.ItemDisplayWrapper(foundItem));

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<ItemRepository> itemRepMock = mockStatic(ItemRepository.class);
             MockedStatic<MessageRepository> msgRepMock = mockStatic(MessageRepository.class);
             MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            itemRepMock.when(ItemRepository::getInstance).thenReturn(mockItemRepository);
            msgRepMock.when(MessageRepository::getInstance).thenReturn(mockMessageRepository);

            injectField("usersComboBox", mockUsersComboBox);
            injectField("itemsComboBox", mockItemsComboBox);
            injectField("currentUser", mockCurrentUser);
            injectField("messageRepository", mockMessageRepository);
            injectField("userRepository", mockUserRepository);

            controller.setItem(lostItem, null);

            // Act
            controller.handleClaim();

            // Assert - Verify success alert is shown
            alertMock.verify(() -> AlertUtil.showAlert("Success", "Item claimed successfully!", Alert.AlertType.INFORMATION));
            navMock.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Test 3: HandleClaim shows error when no user selected")
    void testHandleClaimNoUserSelected() {
        // Arrange
        Item lostItem = new LostItem(
                UUID.randomUUID().toString(),
                "Lost Item",
                "Description",
                "Category",
                "Location",
                "user123",
                LocalDateTime.now(),
                0.0
        );

        when(mockCurrentUser.getUserId()).thenReturn("user123");
        when(mockUsersComboBox.getValue()).thenReturn(null);

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<ItemRepository> itemRepMock = mockStatic(ItemRepository.class);
             MockedStatic<MessageRepository> msgRepMock = mockStatic(MessageRepository.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            itemRepMock.when(ItemRepository::getInstance).thenReturn(mockItemRepository);
            msgRepMock.when(MessageRepository::getInstance).thenReturn(mockMessageRepository);

            when(mockMessageRepository.getUsersFromConversations("user123")).thenReturn(Collections.emptyList());

            injectField("usersComboBox", mockUsersComboBox);
            injectField("itemsComboBox", mockItemsComboBox);
            injectField("currentUser", mockCurrentUser);
            injectField("messageRepository", mockMessageRepository);
            injectField("userRepository", mockUserRepository);

            controller.setItem(lostItem, null);

            // Act
            controller.handleClaim();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Please select a user", Alert.AlertType.ERROR));
        }
    }

    @Test
    @DisplayName("Test 4: HandleClaim shows error when no item selected")
    void testHandleClaimUpdateFails() {
        // Arrange
        Item lostItem = new LostItem(
                UUID.randomUUID().toString(),
                "Lost Item",
                "Description",
                "Category",
                "Location",
                "user123",
                LocalDateTime.now(),
                0.0
        );

        when(mockCurrentUser.getUserId()).thenReturn("user123");
        when(mockUsersComboBox.getValue()).thenReturn("user456");
        when(mockItemsComboBox.getValue()).thenReturn(null);  // No item selected

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<ItemRepository> itemRepMock = mockStatic(ItemRepository.class);
             MockedStatic<MessageRepository> msgRepMock = mockStatic(MessageRepository.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepository);
            itemRepMock.when(ItemRepository::getInstance).thenReturn(mockItemRepository);
            msgRepMock.when(MessageRepository::getInstance).thenReturn(mockMessageRepository);

            injectField("usersComboBox", mockUsersComboBox);
            injectField("itemsComboBox", mockItemsComboBox);
            injectField("currentUser", mockCurrentUser);
            injectField("messageRepository", mockMessageRepository);
            injectField("userRepository", mockUserRepository);

            controller.setItem(lostItem, null);

            // Act
            controller.handleClaim();

            // Assert - Verify error alert when no item is selected
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Please select an item", Alert.AlertType.ERROR));
        }
    }
}

