package net.javaguids.lost_and_found.context;

public class ModeratorConversationContext {
    private static String user1Id;
    private static String user2Id;
    private static String itemId;

    public static void setConversation(String u1, String u2, String item) {
        user1Id = u1;
        user2Id = u2;
        itemId = item;
    }

    public static String getUser1Id() {
        return user1Id;
    }

    public static String getUser2Id() {
        return user2Id;
    }

    public static String getItemId() {
        return itemId;
    }

    public static void clear() {
        user1Id = null;
        user2Id = null;
        itemId = null;
    }
}