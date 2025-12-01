package net.javaguids.lost_and_found.context;

public class ConversationContext {
    private static String userId;
    private static String itemId;

    public static void setConversation(String user, String item) {
        userId = user;
        itemId = item;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getItemId() {
        return itemId;
    }

    public static void clear() {
        userId = null;
        itemId = null;
    }
}