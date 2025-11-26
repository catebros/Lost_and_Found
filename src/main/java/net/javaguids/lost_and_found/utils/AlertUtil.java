package net.javaguids.lost_and_found.utils;

import javafx.scene.control.Alert;


//Utility class for displaying alerts in the application

public class AlertUtil {

    //Display an alert dialog
    //- The alert title
    //- The alert message content
    //- The alert type (ERROR, WARNING, INFORMATION, etc.)
    
    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Private constructor to prevent instantiation

}
