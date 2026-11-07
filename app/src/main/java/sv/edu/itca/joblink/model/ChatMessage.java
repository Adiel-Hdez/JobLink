package sv.edu.itca.joblink.model;

public class ChatMessage {
    private String id;
    private String senderId;
    private String text;
    private String time;
    private boolean isMine;

    public ChatMessage(String id, String senderId, String text, String time, boolean isMine) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
        this.isMine = isMine;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public String getTime() { return time; }
    public boolean isMine() { return isMine; }
}