package net.javaguids.lost_and_found.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.model.users.User;
import net.javaguids.lost_and_found.services.AuthService;


import java.io.IOException;

// NavigationManager handles scene navigation in the Lost and Found application.

public class NavigationManager {
    private static Stage primaryStage;
    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Navigates to the specified FXML file with the given title

    public static void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/net/javaguids/lost_and_found/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to: " + fxmlFile);
        }
    }

    // Navigates back to the appropriate dashboard based on the current user's role

    public static void goBack() {
        // Always go back to the appropriate dashboard based on user role
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            String dashboardFile = getDashboardForRole(currentUser.getRole().toString());
            String dashboardTitle = getDashboardTitle(currentUser.getRole().toString());
            navigateTo(dashboardFile, dashboardTitle);
        }
    }

    // Helper methods to get dashboard file and title based on user role

    private static String getDashboardForRole(String role) {
        switch (role) {
            case "ADMIN":
                return "admin-dashboard-view.fxml";
            case "MODERATOR":
                return "moderator-dashboard-view.fxml";
            default:
                return "user-dashboard-view.fxml";
        }
    }

    // Helper method to get dashboard title based on user role

    private static String getDashboardTitle(String role) {
        switch (role) {
            case "ADMIN":
                return "Lost and Found - Admin Dashboard";
            case "MODERATOR":
                return "Lost and Found - Moderator Dashboard";
            default:
                return "Lost and Found - Dashboard";
        }
    }
}
