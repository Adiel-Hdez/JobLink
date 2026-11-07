package sv.edu.itca.joblink.model;

public class ChatConversation {
    private String id;
    private String contactName;
    private String contactProfession;
    private String lastMessage;
    private String time;
    private int unreadCount;

    public ChatConversation(String id, String contactName, String contactProfession, String lastMessage, String time, int unreadCount) {
        this.id = id;
        this.contactName = contactName;
        this.contactProfession = contactProfession;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
    }

    public String getId() { return id; }
    public String getContactName() { return contactName; }
    public String getContactProfession() { return contactProfession; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}