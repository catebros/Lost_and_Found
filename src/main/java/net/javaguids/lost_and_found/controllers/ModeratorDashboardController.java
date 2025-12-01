package net.javaguids.lost_and_found.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.database.ItemRepository;
import net.javaguids.lost_and_found.database.MessageRepository;
import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.utils.AlertUtil;
import net.javaguids.lost_and_found.context.ModeratorConversationContext;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ModeratorDashboardController {
    @FXML
    private TableView<Item> itemsTable;

    @FXML
    private TableView<ConversationRow> messagesTable;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalChatsLabel;

    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private MessageRepository messageRepository;

    private static class ConversationRow {
        String conversationName;
        String lastMessage;
        String timestamp;
        String user1Id;
        String user2Id;
        String itemId;

        ConversationRow(String conversationName, String lastMessage, String timestamp, String user1Id, String user2Id, String itemId) {
            this.conversationName = conversationName;
            this.lastMessage = lastMessage;
            this.timestamp = timestamp;
            this.user1Id = user1Id;
            this.user2Id = user2Id;
            this.itemId = itemId;
        }
    }

    @FXML
    public void initialize() {
        userRepository = UserRepository.getInstance();
        itemRepository = ItemRepository.getInstance();
        messageRepository = MessageRepository.getInstance();

        setupItemsTable();
        setupMessagesTable();

        loadItems();
        loadConversations();
        loadStatistics();
    }

    private void setupItemsTable() {
        itemsTable.getColumns().clear();

        TableColumn<Item, String> titleCol = new TableColumn<>("Title");
        titleCol.setPrefWidth(130);
        titleCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

        TableColumn<Item, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setPrefWidth(100);
        categoryCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Item, String> locationCol = new TableColumn<>("Location");
        locationCol.setPrefWidth(130);
        locationCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));

        TableColumn<Item, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(70);
        typeCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));

        TableColumn<Item, String> postedByCol = new TableColumn<>("Posted By");
        postedByCol.setPrefWidth(120);
        postedByCol.setCellValueFactory(cellData -> {
            User user = userRepository.getUserById(cellData.getValue().getPostedByUserId());
            String username = user != null ? user.getUsername() : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(username);
        });

        TableColumn<Item, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(90);
        statusCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));

        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(param -> new TableCell<Item, Void>() {
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        itemsTable.getColumns().addAll(titleCol, categoryCol, locationCol, typeCol, postedByCol, statusCol, actionsCol);
    }

    private void setupMessagesTable() {
        messagesTable.getColumns().clear();

        TableColumn<ConversationRow, String> conversationCol = new TableColumn<>("Conversation");
        conversationCol.setPrefWidth(250);
        conversationCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().conversationName));

        TableColumn<ConversationRow, String> lastMessageCol = new TableColumn<>("Last Message");
        lastMessageCol.setPrefWidth(300);
        lastMessageCol.setCellValueFactory(cellData -> {
            String message = cellData.getValue().lastMessage;
            if (message.length() > 50) {
                return new javafx.beans.property.SimpleStringProperty(message.substring(0, 50) + "...");
            }
            return new javafx.beans.property.SimpleStringProperty(message);
        });

        TableColumn<ConversationRow, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setPrefWidth(150);
        timestampCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().timestamp));

        TableColumn<ConversationRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(param -> new TableCell<ConversationRow, Void>() {
            private final Button viewBtn = new Button("View");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, viewBtn);

            {
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                viewBtn.setOnAction(event -> {
                    ConversationRow conversation = getTableView().getItems().get(getIndex());
                    handleViewConversation(conversation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        messagesTable.getColumns().addAll(conversationCol, lastMessageCol, timestampCol, actionsCol);
    }

    private void loadItems() {
        //TODO: FIX
        //List<Item> items = itemRepository.searchItems(null);
        //ObservableList<Item> observableItems = FXCollections.observableArrayList(items);
        //itemsTable.setItems(observableItems);
    }

    private void loadConversations() {
        List<Message> allMessages = messageRepository.getAllMessages();

        Map<String, Message> conversationsMap = new HashMap<>();

        for (Message msg : allMessages) {
            String user1 = msg.getSenderId();
            String user2 = msg.getReceiverId();

            // Create a consistent key for the conversation
            String key = createConversationKey(user1, user2, null);

            // Keep the most recent message for each conversation
            if (!conversationsMap.containsKey(key) ||
                    msg.getTimestamp().isAfter(conversationsMap.get(key).getTimestamp())) {
                conversationsMap.put(key, msg);
            }
        }

        List<ConversationRow> conversations = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Map.Entry<String, Message> entry : conversationsMap.entrySet()) {
            Message msg = entry.getValue();

            String user1Id = msg.getSenderId();
            String user2Id = msg.getReceiverId();

            // Get usernames
            String user1Name = getUserDisplayName(user1Id);
            String user2Name = getUserDisplayName(user2Id);

            // Create conversation name with both users only (no item in the name)
            String conversationName = user1Name + " <→> " + user2Name;

            String timestamp = msg.getTimestamp().format(formatter);
            ConversationRow row = new ConversationRow(conversationName, msg.getContent(), timestamp, user1Id, user2Id, null);
            conversations.add(row);
        }

        // Sort by timestamp (newest first)
        conversations.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

        ObservableList<ConversationRow> observableConversations = FXCollections.observableArrayList(conversations);
        messagesTable.setItems(observableConversations);
    }

    private String createConversationKey(String user1, String user2, String itemId) {
        // Create key based on users only, not by item
        // This means all messages between two users are in one conversation
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private String getUserDisplayName(String userId) {
        if (userId.equals("SYSTEM")) {
            return "Lost and Found Team";
        }
        User user = userRepository.getUserById(userId);
        return user != null ? user.getUsername() : "Unknown";
    }

    private void loadStatistics() {
        //TODO: FIX
        //List<Item> items = itemRepository.searchItems(null);
        //totalItemsLabel.setText("Total Items: " + items.size());

        List<Message> allMessages = messageRepository.getAllMessages();
        Set<String> uniqueConversations = new HashSet<>();

        for (Message msg : allMessages) {
            String user1 = msg.getSenderId();
            String user2 = msg.getReceiverId();
            String key = createConversationKey(user1, user2, null);
            uniqueConversations.add(key);
        }

        totalChatsLabel.setText("Total Conversations: " + uniqueConversations.size());
    }

    private void handleDeleteItem(Item item) {
        if (item == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Item");
        confirmation.setContentText("Are you sure you want to delete item: " + item.getTitle() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = itemRepository.deleteItem(item.getItemId());
                if (success) {
                    AlertUtil.showAlert("Success", "Item deleted successfully", Alert.AlertType.INFORMATION);
                    loadItems();
                    loadConversations();
                    loadStatistics();
                } else {
                    AlertUtil.showAlert("Error", "Failed to delete item", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void handleViewConversation(ConversationRow conversation) {
        // Store the conversation details before navigating
        ModeratorConversationContext.setConversation(conversation.user1Id, conversation.user2Id, conversation.itemId);
        NavigationManager.navigateTo("messages-view.fxml", "Conversation: " + conversation.conversationName);
    }

    @FXML
    public void handleGoBack() {
        NavigationManager.goBack();
    }

    @FXML
    public void handleLogout() {
        AuthService.logout();
        NavigationManager.navigateTo("login-view.fxml", "Lost and Found - Login");
    }
}
