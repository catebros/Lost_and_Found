package net.javaguids.lost_and_found;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.javaguids.lost_and_found.utils.NavigationManager; // Import NavigationManager for managing scenes

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    // The main entry point for all JavaFX applications
    public void start(Stage stage) throws IOException {
        // Set the primary stage in NavigationManager for single-window navigation
        NavigationManager.setPrimaryStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml")); // Load the FXML file
        Scene scene = new Scene(fxmlLoader.load(), 400, 400); // Create a scene
        stage.setTitle("Lost and Found - Login"); // Set the window title
        stage.setScene(scene); // Set the scene to the stage
        stage.show();
    }
}