package net.javaguids.lost_and_found.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.messaging.Message;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.MessageService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.context.ItemDetailsContext;
import net.javaguids.lost_and_found.context.NavigationContext;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ItemDetailsController {
    @FXML
    private Label titleLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label rewardLabel;

    @FXML
    private Label postedByLabel;

    @FXML
    private ImageView imageView;

    private Item currentItem;
    private MessageService messageService;

    @FXML
    public void initialize() {
        messageService = new MessageService();

        // Get item from context if set
        Item contextItem = ItemDetailsContext.getItem();
        if (contextItem != null) {
            setItem(contextItem);
            ItemDetailsContext.clear();
        }
    }

    public void setItem(Item item) {
        this.currentItem = item;

        titleLabel.setText(item.getTitle());
        descriptionArea.setText(item.getDescription());
        categoryLabel.setText("Category: " + item.getCategory());
        locationLabel.setText("Location: " + item.getLocation());
        typeLabel.setText("Type: " + item.getType().toString());

        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            dateLabel.setText("Date Lost: " + lostItem.getDateLost().toLocalDate());
            rewardLabel.setText("Reward: $" + lostItem.getReward());
            rewardLabel.setVisible(true);
        } else if (item instanceof FoundItem) {
            FoundItem foundItem = (FoundItem) item;
            dateLabel.setText("Date Found: " + foundItem.getDateFound().toLocalDate());
            rewardLabel.setVisible(false);
        }

        User postedByUser = net.javaguids.lost_and_found.database.UserRepository.getInstance().getUserById(item.getPostedByUserId());
        String username = postedByUser != null ? postedByUser.getUsername() : "Unknown User";
        postedByLabel.setText("Posted by: " + username);

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            File imageFile = new File(item.getImagePath());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            }
        }

        // Note: Item claiming is now done through the Messages interface
    }

    @FXML
    public void handleGoBack() {
        String previousPage = NavigationContext.getPreviousPage();
        String previousTitle = NavigationContext.getPreviousTitle();
        NavigationContext.clear();

        if (previousPage != null && previousTitle != null) {
            NavigationManager.navigateTo(previousPage, previousTitle);
        } else {
            // Fallback to dashboard if no previous page stored
            NavigationManager.goBack();
        }
    }

    @FXML
    public void handleSendMessage() {
        if (currentItem == null) {
            return;
        }

        String otherUserId = currentItem.getPostedByUserId();

        // Set conversation context to auto-open the conversation with this user
        net.javaguids.lost_and_found.context.ConversationContext.setConversation(otherUserId, null);

        // Navigate to messages view
        NavigationManager.navigateTo("messages-view.fxml", "Messages");
    }
}