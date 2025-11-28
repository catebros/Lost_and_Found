package net.javaguids.lost_and_found.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import net.javaguids.lost_and_found.context.ItemDetailsContext;
import net.javaguids.lost_and_found.context.NavigationContext;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.search.SearchCriteria;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SearchControllerTest {

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // already started
        }
    }

    private SearchController controller;

    @Mock
    private ItemService mockItemService;

    @Mock
    private TextField mockKeywordsField;

    @Mock
    private ComboBox<String> mockCategoryCombo;

    @Mock
    private TextField mockLocationField;

    @Mock
    private ComboBox<String> mockTypeCombo;

    @Mock
    private TableView<Item> mockResultsTable;

    @Mock
    private TableColumn<Item, String> mockTitleColumn;

    @Mock
    private TableColumn<Item, String> mockCategoryColumn;

    @Mock
    private TableColumn<Item, String> mockLocationColumn;

    @Mock
    private TableColumn<Item, String> mockTypeColumn;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SearchController();
        injectField("keywordsField", mockKeywordsField);
        injectField("categoryCombo", mockCategoryCombo);
        injectField("locationField", mockLocationField);
        injectField("typeCombo", mockTypeCombo);
        injectField("resultsTable", mockResultsTable);
        injectField("titleColumn", mockTitleColumn);
        injectField("categoryColumn", mockCategoryColumn);
        injectField("locationColumn", mockLocationColumn);
        injectField("typeColumn", mockTypeColumn);
        // common stubbing
        when(mockCategoryCombo.getItems()).thenReturn(FXCollections.observableArrayList());
        when(mockTypeCombo.getItems()).thenReturn(FXCollections.observableArrayList());
        doNothing().when(mockTitleColumn).setCellValueFactory(any());
        doNothing().when(mockCategoryColumn).setCellValueFactory(any());
        doNothing().when(mockLocationColumn).setCellValueFactory(any());
        doNothing().when(mockTypeColumn).setCellValueFactory(any());
        doNothing().when(mockResultsTable).setOnMouseClicked(any());
    }

    @Test
    @DisplayName("initialize populates dropdowns and sets defaults")
    void testInitializeSetsUpDropdowns() {
        ObservableList<String> categories = FXCollections.observableArrayList();
        ObservableList<String> types = FXCollections.observableArrayList();
        when(mockCategoryCombo.getItems()).thenReturn(categories);
        when(mockTypeCombo.getItems()).thenReturn(types);

        controller.initialize();

        assertTrue(categories.containsAll(
            FXCollections.observableArrayList("All", "Electronics", "Clothing", "Accessories", "Documents", "Keys", "Books", "Bags", "Other")),
            "Category list should contain predefined values");
        assertEquals(9, categories.size(), "Category list should have 9 entries");
        assertTrue(types.containsAll(FXCollections.observableArrayList("All", "LOST", "FOUND")));
        assertEquals(3, types.size(), "Type list should have 3 entries");
        verify(mockCategoryCombo).setValue("All");
        verify(mockTypeCombo).setValue("All");
        verify(mockResultsTable).setOnMouseClicked(any());
    }

    @Test
    @DisplayName("handleSearch builds criteria from fields and updates table")
    void testHandleSearchBuildsCriteria() {
        when(mockKeywordsField.getText()).thenReturn("wallet");
        when(mockCategoryCombo.getValue()).thenReturn("Accessories");
        when(mockLocationField.getText()).thenReturn("park");
        when(mockTypeCombo.getValue()).thenReturn("LOST");
        injectField("itemService", mockItemService);
        Item mockItem = mock(Item.class);
        when(mockItemService.searchItemsExcludingUser(any(SearchCriteria.class), eq("user1")))
            .thenReturn(List.of(mockItem));

        try (MockedStatic<net.javaguids.lost_and_found.services.AuthService> authMock = mockStatic(net.javaguids.lost_and_found.services.AuthService.class)) {
            var mockUser = mock(net.javaguids.lost_and_found.model.users.User.class);
            authMock.when(net.javaguids.lost_and_found.services.AuthService::getCurrentUser).thenReturn(mockUser);
            when(mockUser.getUserId()).thenReturn("user1");

            controller.handleSearch();
        }

        ArgumentCaptor<SearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(mockItemService).searchItemsExcludingUser(criteriaCaptor.capture(), eq("user1"));
        SearchCriteria criteria = criteriaCaptor.getValue();
        assertEquals("wallet", criteria.getKeywords());
        assertEquals("Accessories", criteria.getCategory());
        assertEquals("park", criteria.getLocation());
        assertEquals(ItemType.LOST, criteria.getType());

        ArgumentCaptor<ObservableList<Item>> resultsCaptor = ArgumentCaptor.forClass(ObservableList.class);
        verify(mockResultsTable).setItems(resultsCaptor.capture());
        assertEquals(1, resultsCaptor.getValue().size(), "Results table should receive returned items");
    }

    @Test
    @DisplayName("handleSearch ignores empty and 'All' filters")
    void testHandleSearchIgnoresEmptyFilters() {
        when(mockKeywordsField.getText()).thenReturn("");
        when(mockCategoryCombo.getValue()).thenReturn("All");
        when(mockLocationField.getText()).thenReturn("");
        when(mockTypeCombo.getValue()).thenReturn("All");
        injectField("itemService", mockItemService);
        when(mockItemService.searchItemsExcludingUser(any(SearchCriteria.class), eq("u1")))
            .thenReturn(List.of());

        try (MockedStatic<net.javaguids.lost_and_found.services.AuthService> authMock = mockStatic(net.javaguids.lost_and_found.services.AuthService.class)) {
            var mockUser = mock(net.javaguids.lost_and_found.model.users.User.class);
            authMock.when(net.javaguids.lost_and_found.services.AuthService::getCurrentUser).thenReturn(mockUser);
            when(mockUser.getUserId()).thenReturn("u1");

            controller.handleSearch();
        }

        ArgumentCaptor<SearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(mockItemService).searchItemsExcludingUser(criteriaCaptor.capture(), eq("u1"));
        SearchCriteria criteria = criteriaCaptor.getValue();
        assertNull(criteria.getKeywords(), "Keywords should remain unset");
        assertNull(criteria.getCategory(), "Category should remain unset");
        assertNull(criteria.getLocation(), "Location should remain unset");
        assertNull(criteria.getType(), "Type should remain unset");

        ArgumentCaptor<ObservableList<Item>> resultsCaptor = ArgumentCaptor.forClass(ObservableList.class);
        verify(mockResultsTable).setItems(resultsCaptor.capture());
        assertEquals(0, resultsCaptor.getValue().size(), "Results should be empty");
    }

    @Test
    @DisplayName("handleGoBack delegates to NavigationManager")
    void testHandleGoBack() {
        try (MockedStatic<NavigationManager> navMock = mockStatic(NavigationManager.class)) {
            controller.handleGoBack();
            navMock.verify(NavigationManager::goBack);
        }
    }

    @Test
    @DisplayName("handleViewItem stores context and navigates to details")
    void testHandleViewItem() {
        Item mockItem = mock(Item.class);
        try (MockedStatic<ItemDetailsContext> detailsMock = mockStatic(ItemDetailsContext.class);
             MockedStatic<NavigationContext> navContextMock = mockStatic(NavigationContext.class);
             MockedStatic<NavigationManager> navManagerMock = mockStatic(NavigationManager.class)) {

            controller.handleViewItem(mockItem);

            detailsMock.verify(() -> ItemDetailsContext.setItem(mockItem));
            navContextMock.verify(() -> NavigationContext.setPreviousPage("search-view.fxml", "Search Items"));
            navManagerMock.verify(() -> NavigationManager.navigateTo("item-details-view.fxml", "Item Details"));
        }
    }

    private void injectField(String fieldName, Object value) {
        try {
            var field = SearchController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            fail("Failed to inject field: " + fieldName + " - " + e.getMessage());
        }
    }
}
