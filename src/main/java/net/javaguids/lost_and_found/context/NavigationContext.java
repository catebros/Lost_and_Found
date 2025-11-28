package net.javaguids.lost_and_found.context;

// Context for tracking navigation origin

public class NavigationContext {
    private static String previousPage;
    private static String previousTitle;

    // Sets the previous page information for navigation context.
    public static void setPreviousPage(String fxmlFile, String title) {
        previousPage = fxmlFile;
        previousTitle = title;
    }

    // Gets the FXML file path of the previous page.
    public static String getPreviousPage() {
        return previousPage;
    }

    // Gets the title of the previous page.
    public static String getPreviousTitle() {
        return previousTitle;
    }

    // Clears the navigation context, removing all stored previous page information.
    public static void clear() {
        previousPage = null;
        previousTitle = null;
    }
}
