package sv.edu.itca.joblink.model;

public class ChatMessage {
    private String id;
    private String senderId;
    private String text;
    private String time;
    private boolean isMine;
    private String imageAttachmentUrl;
    private boolean isRead;

    public ChatMessage(String id, String senderId, String text, String time, boolean isMine) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
        this.isMine = isMine;
    }

    public ChatMessage(String id, String senderId, String text, String time, boolean isMine, String imageAttachmentUrl, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
        this.isMine = isMine;
        this.imageAttachmentUrl = imageAttachmentUrl;
        this.isRead = isRead;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public String getTime() { return time; }
    public boolean isMine() { return isMine; }
    public String getImageAttachmentUrl() { return imageAttachmentUrl; }
    public void setImageAttachmentUrl(String imageAttachmentUrl) { this.imageAttachmentUrl = imageAttachmentUrl; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
