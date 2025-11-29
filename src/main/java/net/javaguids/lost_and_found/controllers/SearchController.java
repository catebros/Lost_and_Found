package net.javaguids.lost_and_found.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.javaguids.lost_and_found.model.enums.ItemType;
import net.javaguids.lost_and_found.model.items.Item;
import net.javaguids.lost_and_found.search.SearchCriteria;
import net.javaguids.lost_and_found.services.ItemService;
import net.javaguids.lost_and_found.utils.NavigationManager;
import net.javaguids.lost_and_found.context.ItemDetailsContext;
import net.javaguids.lost_and_found.context.NavigationContext;

import java.io.IOException;
import java.util.List;

// Controller for the search view, handling user input and displaying search results.
// Features: multi-cirteria search (keywords, category, location, type), table display of search results, double-click navigation to item details and navigation back to previous view.

public class SearchController {
    // FMXL UI COMPONENTS 

    // text field for entering search keywords
    @FXML
    private TextField keywordsField;

    // dropdown for selecting item category filter
    @FXML
    private ComboBox<String> categoryCombo;

    // text field for entering location filter
    @FXML
    private TextField locationField;

    // dropdown for selecting item type filter (LOST/FOUND)
    @FXML
    private ComboBox<String> typeCombo;

    // table for displaying search results
    @FXML
    private TableView<Item> resultsTable;


    // TABLE COLUMNS 

    // table columns for displaying item titles
    @FXML
    private TableColumn<Item, String> titleColumn;
     
    // table column for displaying item categories
    @FXML
    private TableColumn<Item, String> categoryColumn;

    // table column for displaying item locations
    @FXML
    private TableColumn<Item, String> locationColumn;

    // table column for displaying item types (LOST/FOUND)
    @FXML
    private TableColumn<Item, String> typeColumn;

    // SERVICE DEPENDENCIES 

    // Service for handling item related operations
    private ItemService itemService;

    // Initialization method called after FXML components are loaded
    // Sets up UI components, populates dropdown menus, configures table columns, and sets up event handlers for user interactions.
    @FXML
    public void initialize() {
        // Initialize the item service
        itemService = new ItemService();

        // populate category dropdown with predefined categories
        categoryCombo.getItems().addAll(
            "All", "Electronics", "Clothing", "Accessories", "Documents",
            "Keys", "Books", "Bags", "Other"
        );
        categoryCombo.setValue("All");  // Set default selection

        // Populate type dropdown with item types
        typeCombo.getItems().addAll("All", "LOST", "FOUND");
        typeCombo.setValue("All"); 


        // Connect table columns to their corresponding Item data fields
        titleColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        categoryColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        locationColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));
        typeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));

        // Set up double-click event on table rows to view item details
        resultsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Item selectedItem = resultsTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    handleViewItem(selectedItem);
                }
            }
        });
    }

    // Handles going back button click to navigate to the previous view
    @FXML
    public void handleGoBack() {
        NavigationManager.goBack();
    }

    // Handles the search button click or search action
    // Collects search criteria from UI components, performs the search and updates the results table with found items.
    @FXML
    public void handleSearch() {
        // Create search criteria object
        SearchCriteria criteria = new SearchCriteria();

        // Collect keywords from text field if provided 
        String keywords = keywordsField.getText();
        if (!keywords.isEmpty()) {
            criteria.setKeywords(keywords);
        }

        // Set category filter if a specific category is selected
        String category = categoryCombo.getValue();
        if (category != null && !category.equals("All")) {
            criteria.setCategory(category);
        }

        // Collect location from text field if provided
        String location = locationField.getText();
        if (!location.isEmpty()) {
            criteria.setLocation(location);
        }

        // Set type filter if a specific type is selected
        String type = typeCombo.getValue();
        if (type != null && !type.equals("All")) {
            criteria.setType(ItemType.valueOf(type));
        }

        // Ger current user ID to exclude their items from search results
        String currentUserId = net.javaguids.lost_and_found.services.AuthService.getCurrentUser().getUserId();
        // Perform search using item service
        List<Item> results = itemService.searchItemsExcludingUser(criteria, currentUserId);
        // convert results to observable list for table binding 
        ObservableList<Item> observableResults = FXCollections.observableArrayList(results);
        // update table with search results
        resultsTable.setItems(observableResults);
    }

    // Handles viewing item details when an item is double-clicked in the results table
    public void handleViewItem(Item item) {
        // Store the item in the details context for the next view
        ItemDetailsContext.setItem(item);
        // Set up navigation context to enable proper back navigation
        NavigationContext.setPreviousPage("search-view.fxml", "Search Items");
        // Navigate to the item details view
        NavigationManager.navigateTo("item-details-view.fxml", "Item Details");
    }
}
