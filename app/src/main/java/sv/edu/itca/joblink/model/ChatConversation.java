package sv.edu.itca.joblink.model;

public class ChatConversation {
    private String id;
    private String contactId;
    private String contactName;
    private String contactProfession;
    private String contactPhotoUrl;
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

    public ChatConversation(String id, String contactId, String contactName, String contactProfession, String contactPhotoUrl, String lastMessage, String time, int unreadCount) {
        this.id = id;
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactProfession = contactProfession;
        this.contactPhotoUrl = contactPhotoUrl;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
    }

    public String getId() { return id; }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public String getContactProfession() { return contactProfession; }
    public String getContactPhotoUrl() { return contactPhotoUrl; }
    public void setContactPhotoUrl(String contactPhotoUrl) { this.contactPhotoUrl = contactPhotoUrl; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}