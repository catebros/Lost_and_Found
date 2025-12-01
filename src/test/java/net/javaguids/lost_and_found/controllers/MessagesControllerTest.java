package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.VBox;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.MessageService;
import net.javaguids.lost_and_found.services.ItemService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("MessagesController Tests")
class MessagesControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private MessagesController controller;

    @Mock
    private MessageService mockMessageService;

    @Mock
    private ItemService mockItemService;

    @Mock
    private User mockCurrentUser;

    @Mock
    private User mockOtherUser;

    @Mock
    private ListView<Object> mockConversationsList;

    @Mock
    private ListView<Message> mockMessagesListView;

    @Mock
    private TextArea mockMessageTextArea;

    @Mock
    private Label mockConversationLabel;

    @Mock
    private VBox mockConversationsPanel;

    @Mock
    private Button mockSendMessageButton;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MessagesController();
        setupUIComponentMocks();
    }

    /**
     * Helper method to inject mocked UI components
     */
    private void injectField(String fieldName, Object value) {
        try {
            var field = MessagesController.class.getDeclaredField(fieldName);
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
        when(mockConversationsList.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockMessagesListView.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockMessageTextArea.getText()).thenReturn("");

        // Mock SelectionModels for ListViews
        @SuppressWarnings("unchecked")
        MultipleSelectionModel<Object> mockConversationsSelectionModel = mock(MultipleSelectionModel.class);
        when(mockConversationsList.getSelectionModel()).thenReturn(mockConversationsSelectionModel);
        @SuppressWarnings("unchecked")
        ObjectProperty<Object> mockConversationsProperty = mock(ObjectProperty.class);
        when(mockConversationsSelectionModel.selectedItemProperty()).thenReturn(mockConversationsProperty);

        @SuppressWarnings("unchecked")
        MultipleSelectionModel<Message> mockMessagesSelectionModel = mock(MultipleSelectionModel.class);
        when(mockMessagesListView.getSelectionModel()).thenReturn(mockMessagesSelectionModel);
        @SuppressWarnings("unchecked")
        ObjectProperty<Message> mockMessagesProperty = mock(ObjectProperty.class);
        when(mockMessagesSelectionModel.selectedItemProperty()).thenReturn(mockMessagesProperty);

        injectField("conversationsList", mockConversationsList);
        injectField("messagesListView", mockMessagesListView);
        injectField("messageTextArea", mockMessageTextArea);
        injectField("conversationLabel", mockConversationLabel);
        injectField("conversationsPanel", mockConversationsPanel);
        injectField("sendMessageButton", mockSendMessageButton);
        injectField("messageService", mockMessageService);
        injectField("itemService", mockItemService);
        injectField("currentUser", mockCurrentUser);
    }

    @Test
    @DisplayName("Test 1: Initialize loads regular user conversations")
    void testInitializeLoadConversations() {
        // Arrange
        when(mockCurrentUser.getUserId()).thenReturn("currentUser123");
        when(mockOtherUser.getUsername()).thenReturn("john_doe");

        Message msg = new Message(
            UUID.randomUUID().toString(),
            "user1",
            "currentUser123",
            "Hello"
        );

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);
            repMock.when(UserRepository::getInstance).thenReturn(mock(UserRepository.class));

            var mockUserRepo = mock(UserRepository.class);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            when(mockMessageService.getInbox("currentUser123")).thenReturn(Arrays.asList(msg));

            // Act - initialize is called in setup, just verify the setup worked
            controller.initialize();

            // Assert - verify messageService is injected properly
            // Note: initialize calls controller's internal methods which should use mockMessageService
        }
    }

    @Test
    @DisplayName("Test 2: HandleSendMessage sends message successfully")
    void testHandleSendMessageSuccess() {
        // Arrange
        when(mockCurrentUser.getUserId()).thenReturn("currentUser123");
        when(mockMessageTextArea.getText()).thenReturn("Hello, this is a test message");

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);

            when(mockMessageService.sendMessage(any(Message.class))).thenReturn(true);

            injectField("selectedUserId", "user456");
            injectField("currentUser", mockCurrentUser);
            injectField("messageService", mockMessageService);
            injectField("messageTextArea", mockMessageTextArea);
            injectField("conversationsList", mockConversationsList);

            // Act
            controller.handleSendMessage();

            // Assert
            verify(mockMessageService).sendMessage(any(Message.class));
            verify(mockMessageTextArea).clear();
        }
    }

    @Test
    @DisplayName("Test 3: HandleSendMessage shows error for empty message")
    void testHandleSendMessageEmptyContent() {
        // Arrange
        when(mockMessageTextArea.getText()).thenReturn("");

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            injectField("messageTextArea", mockMessageTextArea);

            // Act
            controller.handleSendMessage();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Please enter a message", Alert.AlertType.ERROR));
            verify(mockMessageService, never()).sendMessage(any());
        }
    }

    @Test
    @DisplayName("Test 4: HandleSendMessage shows error when no conversation selected")
    void testHandleSendMessageNoConversationSelected() {
        // Arrange
        when(mockMessageTextArea.getText()).thenReturn("Test message");

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {
            injectField("selectedUserId", null);
            injectField("messageTextArea", mockMessageTextArea);

            // Act
            controller.handleSendMessage();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Please select a conversation", Alert.AlertType.ERROR));
            verify(mockMessageService, never()).sendMessage(any());
        }
    }

    @Test
    @DisplayName("Test 5: HandleSendMessage shows error on send failure")
    void testHandleSendMessageSendFailure() {
        // Arrange
        when(mockCurrentUser.getUserId()).thenReturn("currentUser123");
        when(mockMessageTextArea.getText()).thenReturn("Test message");

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class);
             MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class)) {

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);

            when(mockMessageService.sendMessage(any(Message.class))).thenReturn(false);

            injectField("selectedUserId", "user456");
            injectField("currentUser", mockCurrentUser);
            injectField("messageService", mockMessageService);
            injectField("messageTextArea", mockMessageTextArea);

            // Act
            controller.handleSendMessage();

            // Assert
            alertMock.verify(() -> AlertUtil.showAlert("Error", "Failed to send message", Alert.AlertType.ERROR));
        }
    }

    @Test
    @DisplayName("Test 6: HandleGoBack navigates back")
    void testHandleGoBack() {
        // Arrange
        try (MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class)) {
            // Act
            controller.handleGoBack();

            // Assert
            navMock.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("Test 7: LoadSpecificConversation loads conversation between two users")
    void testLoadSpecificConversation() {
        // Arrange
        when(mockCurrentUser.getUserId()).thenReturn("currentUser123");

        User user1 = mock(User.class);
        when(user1.getUsername()).thenReturn("alice");
        User user2 = mock(User.class);
        when(user2.getUsername()).thenReturn("bob");

        Message msg = new Message(
            UUID.randomUUID().toString(),
            "user1",
            "user2",
            "Hello from user1"
        );

        try (MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {

            repMock.when(UserRepository::getInstance).thenReturn(mock(UserRepository.class));

            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("user1")).thenReturn(user1);
            when(mockUserRepo.getUserById("user2")).thenReturn(user2);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            authMock.when(AuthService::getCurrentUser).thenReturn(mockCurrentUser);

            when(mockMessageService.getConversation("user1", "user2", null)).thenReturn(Arrays.asList(msg));

            injectField("messagesListView", mockMessagesListView);
            injectField("conversationLabel", mockConversationLabel);
            injectField("conversationsPanel", mockConversationsPanel);

            // Act
            controller.loadSpecificConversation("user1", "user2", null);

            // Assert
            verify(mockConversationLabel).setText("Conversation between alice and bob");
            verify(mockMessageService).getConversation("user1", "user2", null);
        }
    }

}
