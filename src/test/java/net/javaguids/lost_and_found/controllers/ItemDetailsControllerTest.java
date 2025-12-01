package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import net.javaguids.lost_and_found.database.UserRepository;
import net.javaguids.lost_and_found.model.enums.ItemStatus;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.model.items.LostItem;
import net.javaguids.lost_and_found.model.items.FoundItem;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;
import net.javaguids.lost_and_found.services.MessageService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.context.ItemDetailsContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ItemDetailsController Tests")
class ItemDetailsControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    private ItemDetailsController controller;

    @Mock
    private Label mockTitleLabel;

    @Mock
    private TextArea mockDescriptionArea;

    @Mock
    private Label mockCategoryLabel;

    @Mock
    private Label mockLocationLabel;

    @Mock
    private Label mockTypeLabel;

    @Mock
    private Label mockDateLabel;

    @Mock
    private Label mockRewardLabel;

    @Mock
    private Label mockPostedByLabel;

    @Mock
    private ImageView mockImageView;

    @Mock
    private MessageService mockMessageService;

    @Mock
    private User mockPostedByUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ItemDetailsController();
        setupUIComponentMocks();
    }

    /**
     * Helper method to inject mocked UI components
     */
    private void injectField(String fieldName, Object value) {
        try {
            var field = ItemDetailsController.class.getDeclaredField(fieldName);
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
        injectField("titleLabel", mockTitleLabel);
        injectField("descriptionArea", mockDescriptionArea);
        injectField("categoryLabel", mockCategoryLabel);
        injectField("locationLabel", mockLocationLabel);
        injectField("typeLabel", mockTypeLabel);
        injectField("dateLabel", mockDateLabel);
        injectField("rewardLabel", mockRewardLabel);
        injectField("postedByLabel", mockPostedByLabel);
        injectField("imageView", mockImageView);
        injectField("messageService", mockMessageService);
    }

    @Test
    @DisplayName("Test 1: Initialize retrieves item from context")
    void testInitializeGetsItemFromContext() {
        // Arrange
        LostItem lostItem = new LostItem(
            UUID.randomUUID().toString(),
            "Lost Item",
            "Description",
            "Category",
            "Location",
            "user123",
            LocalDateTime.now(),
            50.0
        );
        lostItem.setStatus(ItemStatus.ACTIVE);

        when(mockPostedByUser.getUsername()).thenReturn("john_doe");

        try (MockedStatic<ItemDetailsContext> contextMock = mockStatic(ItemDetailsContext.class);
             MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {

            contextMock.when(ItemDetailsContext::getItem).thenReturn(lostItem);
            repMock.when(UserRepository::getInstance).thenReturn(mock(UserRepository.class));

            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("user123")).thenReturn(mockPostedByUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            // Act
            controller.initialize();

            // Assert
            contextMock.verify(ItemDetailsContext::getItem);
            contextMock.verify(ItemDetailsContext::clear);
        }
    }

    @Test
    @DisplayName("Test 2: SetItem displays LostItem details correctly")
    void testSetItemDisplaysLostItemDetails() {
        // Arrange
        LocalDateTime dateLost = LocalDateTime.of(2024, 11, 20, 10, 30);
        LostItem lostItem = new LostItem(
            UUID.randomUUID().toString(),
            "Lost Wallet",
            "Brown leather wallet with cards",
            "Accessories",
            "Downtown Park",
            "user123",
            dateLost,
            100.0
        );
        lostItem.setStatus(ItemStatus.ACTIVE);

        when(mockPostedByUser.getUsername()).thenReturn("john_doe");

        try (MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {
            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("user123")).thenReturn(mockPostedByUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            // Act
            controller.setItem(lostItem);

            // Assert
            verify(mockTitleLabel).setText("Lost Wallet");
            verify(mockDescriptionArea).setText("Brown leather wallet with cards");
            verify(mockCategoryLabel).setText("Category: Accessories");
            verify(mockLocationLabel).setText("Location: Downtown Park");
            verify(mockTypeLabel).setText("Type: LOST");
            verify(mockRewardLabel).setText("Reward: $100.0");
            verify(mockRewardLabel).setVisible(true);
            verify(mockPostedByLabel).setText("Posted by: john_doe");
        }
    }

    @Test
    @DisplayName("Test 3: SetItem displays FoundItem details correctly")
    void testSetItemDisplaysFoundItemDetails() {
        // Arrange
        LocalDateTime dateFound = LocalDateTime.of(2024, 11, 22, 14, 45);
        FoundItem foundItem = new FoundItem(
            UUID.randomUUID().toString(),
            "Found Phone",
            "Black smartphone at bus station",
            "Electronics",
            "Bus Station",
            "user456",
            dateFound
        );
        foundItem.setStatus(ItemStatus.ACTIVE);

        User postedByUser = mock(User.class);
        when(postedByUser.getUsername()).thenReturn("jane_smith");

        try (MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {
            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("user456")).thenReturn(postedByUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            // Act
            controller.setItem(foundItem);

            // Assert
            verify(mockTitleLabel).setText("Found Phone");
            verify(mockDescriptionArea).setText("Black smartphone at bus station");
            verify(mockCategoryLabel).setText("Category: Electronics");
            verify(mockLocationLabel).setText("Location: Bus Station");
            verify(mockTypeLabel).setText("Type: FOUND");
            verify(mockRewardLabel).setVisible(false);
            verify(mockPostedByLabel).setText("Posted by: jane_smith");
        }
    }

    @Test
    @DisplayName("Test 4: SetItem handles unknown poster gracefully")
    void testSetItemUnknownPoster() {
        // Arrange
        FoundItem foundItem = new FoundItem(
            UUID.randomUUID().toString(),
            "Found Item",
            "Description",
            "Category",
            "Location",
            "unknownUser",
            LocalDateTime.now()
        );

        try (MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class)) {
            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("unknownUser")).thenReturn(null);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            // Act
            controller.setItem(foundItem);

            // Assert
            verify(mockPostedByLabel).setText("Posted by: Unknown User");
        }
    }

    @Test
    @DisplayName("Test 6: HandleSendMessage navigates to messages view")
    void testHandleSendMessage() {
        // Arrange
        LostItem lostItem = new LostItem(
            UUID.randomUUID().toString(),
            "Lost Item",
            "Description",
            "Category",
            "Location",
            "user123",
            LocalDateTime.now(),
            0.0
        );

        when(mockPostedByUser.getUsername()).thenReturn("john_doe");

        try (MockedStatic<UserRepository> repMock = mockStatic(UserRepository.class);
             MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class)) {
            var mockUserRepo = mock(UserRepository.class);
            when(mockUserRepo.getUserById("user123")).thenReturn(mockPostedByUser);
            repMock.when(UserRepository::getInstance).thenReturn(mockUserRepo);

            controller.setItem(lostItem);

            // Act
            controller.handleSendMessage();

            // Assert
            navMock.verify(() -> NavigationManager.navigateTo("messages-view.fxml", "Messages"));
        }
    }

    @Test
    @DisplayName("Test 7: HandleSendMessage does nothing when item is null")
    void testHandleSendMessageWithNullItem() {
        // Arrange - controller with no item set

        try (MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class)) {
            // Act
            controller.handleSendMessage();

            // Assert - no navigation should occur
            navMock.verify(() -> NavigationManager.navigateTo(anyString(), anyString()), never());
        }
    }
}
