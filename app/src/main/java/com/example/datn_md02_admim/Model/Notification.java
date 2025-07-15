package com.example.datn_md02_admim.Model;

public class Notification {
    private String id;
    private String sender;
    private String title;
    private String content;
    private long timestamp;
    private boolean read;
    private String type; // Ví dụ: "message", "reminder", "system"

    public Notification() {}

    public Notification(String id, String sender, String title, String content, long timestamp, boolean read, String type) {
        this.id = id;
        this.sender = sender;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
        this.type = type;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
