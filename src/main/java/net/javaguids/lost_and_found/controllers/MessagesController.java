package net.javaguids.lost_and_found.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.MessageService;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;

import java.util.*;
import java.util.stream.Collectors;

public class MessagesController {
    @FXML
    private ListView<ConversationItem> conversationsList;

    @FXML
    private ListView<Message> messagesListView;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Label conversationLabel;

    @FXML
    private Button sendMessageButton;

    @FXML
    private VBox conversationsPanel;

    private MessageService messageService;
    private ItemService itemService;
    private User currentUser;
    private String selectedUserId;
    private String selectedItemId;
    private boolean isModeratorView = false;
    private int initialMessageCount = 0;
    private String autoOpenedUserId = null;

    private static class ConversationItem {
        String displayName;
        String userId;
        String itemId;
        java.time.LocalDateTime lastMessageTime;

        ConversationItem(String displayName, String userId, String itemId) {
            this.displayName = displayName;
            this.userId = userId;
            this.itemId = itemId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @FXML
    public void initialize() {
        messageService = new MessageService();
        itemService = new ItemService();
        currentUser = AuthService.getCurrentUser();

        // Check if this is a moderator view
        String moderatorUser1Id = net.javaguids.lost_and_found.context.ModeratorConversationContext.getUser1Id();
        String moderatorUser2Id = net.javaguids.lost_and_found.context.ModeratorConversationContext.getUser2Id();

        if (moderatorUser1Id != null && moderatorUser2Id != null) {
            // Load the moderator-specific conversation
            loadSpecificConversation(moderatorUser1Id, moderatorUser2Id, net.javaguids.lost_and_found.context.ModeratorConversationContext.getItemId());
            net.javaguids.lost_and_found.context.ModeratorConversationContext.clear();
        } else {
            // Regular user view - load all conversations
            loadConversations();

            conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedUserId = newVal.userId;
                    selectedItemId = newVal.itemId;
                    loadConversationMessages(newVal);
                }
            });

            // Check if we should auto-open a specific conversation
            String conversationUserId = net.javaguids.lost_and_found.context.ConversationContext.getUserId();
            String conversationItemId = net.javaguids.lost_and_found.context.ConversationContext.getItemId();

            if (conversationUserId != null) {
                // Auto-navigate to the conversation
                navigateToConversation(conversationUserId, conversationItemId);
                net.javaguids.lost_and_found.context.ConversationContext.clear();
            }
        }

        messagesListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    UserRepository userRepo = UserRepository.getInstance();
                    String senderName;

                    if (message.getSenderId().equals("SYSTEM")) {
                        senderName = "Lost and Found Team";
                    } else {
                        User sender = userRepo.getUserById(message.getSenderId());
                        senderName = sender != null ? sender.getUsername() : "Unknown";
                    }

                    String prefix = message.getSenderId().equals(currentUser.getUserId()) ? "You" : senderName;
                    setText(prefix + ": " + message.getContent() + "\n[" + message.getTimestamp().toLocalDate() + " " +
                            message.getTimestamp().toLocalTime().toString().substring(0, 5) + "]");
                }
            }
        });

        // Auto-refresh messages every 2 seconds (but not in moderator view since no new messages will be sent)
        if (!isModeratorView) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                ConversationItem selected = conversationsList.getSelectionModel().getSelectedItem();
                loadConversations();

                // Re-select the previously selected conversation after refresh
                // Conversations are now grouped by user only, so we only check the user ID
                if (selected != null && selectedUserId != null) {
                    for (ConversationItem item : conversationsList.getItems()) {
                        if (item.userId.equals(selected.userId)) {
                            conversationsList.getSelectionModel().select(item);
                            loadConversationMessages(item);
                            break;
                        }
                    }
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    /**
     * Public method to navigate to a specific conversation
     * Called from other controllers (e.g., UserDashboardController)
     */
    public void navigateToConversation(String userId, String itemId) {
        // Track that this conversation was auto-opened
        autoOpenedUserId = userId;

        // Count initial messages in this conversation
        List<Message> existingMessages = messageService.getConversation(
            currentUser.getUserId(),
            userId,
            null
        );
        initialMessageCount = existingMessages.size();

        // First, ensure conversations are loaded
        loadConversations();

        // Find the matching conversation in the list
        // Conversations are now grouped by user only, so we only check the user ID
        boolean found = false;
        for (ConversationItem conversation : conversationsList.getItems()) {
            if (conversation.userId.equals(userId)) {
                // Select this conversation
                conversationsList.getSelectionModel().select(conversation);
                conversationsList.scrollTo(conversation);
                found = true;
                break;
            }
        }

        // If conversation doesn't exist (no messages yet), create a new entry and select it
        if (!found) {
            UserRepository userRepo = UserRepository.getInstance();
            User otherUser = userRepo.getUserById(userId);
            String displayName = otherUser != null ? otherUser.getUsername() : "Unknown User";

            ConversationItem newConversation = new ConversationItem(displayName, userId, null);
            newConversation.lastMessageTime = java.time.LocalDateTime.now();

            // Get current items as a regular list, add the new conversation, and reset
            List<ConversationItem> currentConversations = new ArrayList<>(conversationsList.getItems());
            currentConversations.add(newConversation);
            ObservableList<ConversationItem> updatedList = FXCollections.observableArrayList(currentConversations);
            conversationsList.setItems(updatedList);

            // Select it
            conversationsList.getSelectionModel().select(newConversation);
            conversationsList.scrollTo(newConversation);

            // Manually trigger the selection to load the (empty) conversation
            selectedUserId = userId;
            selectedItemId = null;
            loadConversationMessages(newConversation);
        }
    }

    /**
     * Public method for moderators to load a specific conversation between two users
     * Called from ModeratorDashboardController
     */
    public void loadSpecificConversation(String user1Id, String user2Id, String itemId) {
        // Set moderator view flag
        isModeratorView = true;

        // Hide the conversations list for moderator view
        hideConversationsList();

        // Hide send message button and text area in moderator view
        messageTextArea.setVisible(false);
        messageTextArea.setManaged(false);
        sendMessageButton.setVisible(false);
        sendMessageButton.setManaged(false);

        // Get messages between the two users (load all messages between them, regardless of item)
        List<Message> messages = messageService.getConversation(user1Id, user2Id, null);
        messages.sort(Comparator.comparing(Message::getTimestamp));

        // Set the conversation label with both user names only
        User user1 = net.javaguids.lost_and_found.database.UserRepository.getInstance().getUserById(user1Id);
        User user2 = net.javaguids.lost_and_found.database.UserRepository.getInstance().getUserById(user2Id);
        String user1Name = user1 != null ? user1.getUsername() : "Unknown";
        String user2Name = user2 != null ? user2.getUsername() : "Unknown";

        String labelText = "Conversation between " + user1Name + " and " + user2Name;
        conversationLabel.setText(labelText);

        // Display messages
        ObservableList<Message> observableMessages = FXCollections.observableArrayList(messages);
        messagesListView.setItems(observableMessages);

        if (!messages.isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                messagesListView.scrollTo(messages.size() - 1);
            });
        }
    }

    /**
     * Hide the conversations list sidebar
     */
    private void hideConversationsList() {
        conversationsPanel.setVisible(false);
        conversationsPanel.setManaged(false);
    }

    private void loadConversations() {
        List<Message> allMessages = messageService.getInbox(currentUser.getUserId());

        Map<String, Message> conversationsMap = new HashMap<>();

        for (Message msg : allMessages) {
            String otherUserId = msg.getSenderId().equals(currentUser.getUserId()) ?
                    msg.getReceiverId() : msg.getSenderId();

            // Use only the user ID as the key, not the item ID
            // This means all messages between two users are in one conversation
            String key = otherUserId;

            if (!conversationsMap.containsKey(key) ||
                    msg.getTimestamp().isAfter(conversationsMap.get(key).getTimestamp())) {
                conversationsMap.put(key, msg);
            }
        }

        List<ConversationItem> conversations = new ArrayList<>();
        UserRepository userRepo = UserRepository.getInstance();

        for (Map.Entry<String, Message> entry : conversationsMap.entrySet()) {
            Message msg = entry.getValue();
            String otherUserId = msg.getSenderId().equals(currentUser.getUserId()) ?
                    msg.getReceiverId() : msg.getSenderId();

            String displayName;
            if (otherUserId.equals("SYSTEM")) {
                displayName = "Lost and Found Team";
            } else {
                User otherUser = userRepo.getUserById(otherUserId);
                displayName = otherUser != null ? otherUser.getUsername() : "Unknown User";
            }

            // Do not append item title - conversations are now grouped by user only
            ConversationItem convo = new ConversationItem(displayName, otherUserId, null);
            convo.lastMessageTime = msg.getTimestamp();
            conversations.add(convo);
        }

        // Sort conversations by last message timestamp (newest first)
        conversations.sort((a, b) -> b.lastMessageTime.compareTo(a.lastMessageTime));

        ObservableList<ConversationItem> observableConversations = FXCollections.observableArrayList(conversations);
        conversationsList.setItems(observableConversations);
    }

    private void loadConversationMessages(ConversationItem conversation) {
        conversationLabel.setText("Conversation with " + conversation.displayName);

        // Load all messages between the two users (no item filter)
        List<Message> messages = messageService.getConversation(
                currentUser.getUserId(),
                conversation.userId,
                null
        );

        messages.sort(Comparator.comparing(Message::getTimestamp));

        ObservableList<Message> observableMessages = FXCollections.observableArrayList(messages);
        messagesListView.setItems(observableMessages);

        if (!messages.isEmpty()) {
            // Use Platform.runLater to ensure scroll happens after layout
            javafx.application.Platform.runLater(() -> {
                messagesListView.scrollTo(messages.size() - 1);
            });
        }
    }

    @FXML
    public void handleGoBack() {
        // Check if we need to clean up an empty auto-opened conversation
        if (autoOpenedUserId != null) {
            // Get current message count
            java.util.List<Message> currentMessages = messageService.getConversation(
                currentUser.getUserId(),
                autoOpenedUserId,
                null
            );

            // If no new messages were sent, delete the empty conversation
            if (currentMessages.size() == initialMessageCount && initialMessageCount == 0) {
                messageService.deleteEmptyConversation(currentUser.getUserId(), autoOpenedUserId);
            }
        }

        NavigationManager.goBack();
    }

    @FXML
    public void handleSendMessage() {
        String content = messageTextArea.getText();

        if (content.isEmpty()) {
            AlertUtil.showAlert("Error", "Please enter a message", Alert.AlertType.ERROR);
            return;
        }

        if (selectedUserId == null) {
            AlertUtil.showAlert("Error", "Please select a conversation", Alert.AlertType.ERROR);
            return;
        }

        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, currentUser.getUserId(), selectedUserId, content);

        boolean success = messageService.sendMessage(message);

        if (success) {
            messageTextArea.clear();
            ConversationItem selectedConversation = conversationsList.getSelectionModel().getSelectedItem();
            if (selectedConversation != null) {
                loadConversationMessages(selectedConversation);
            }
        } else {
            AlertUtil.showAlert("Error", "Failed to send message", Alert.AlertType.ERROR);
        }
    }
}