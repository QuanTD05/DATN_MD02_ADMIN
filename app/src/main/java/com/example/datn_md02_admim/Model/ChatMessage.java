package com.example.datn_md02_admim.Model;

public class ChatMessage {
    private String sender;
    private String receiver;
    private String content;     // Dùng cho văn bản
    private String imageUrl;    // Dùng cho ảnh
    private long timestamp;
    private boolean unread;
    private boolean isImage;

    public ChatMessage() {} // Required for Firebase

    public ChatMessage(String sender, String receiver, String contentOrImageUrl, boolean isImage, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.isImage = isImage;
        this.unread = true;

        if (isImage) {
            this.imageUrl = contentOrImageUrl;
            this.content = null;
        } else {
            this.content = contentOrImageUrl;
            this.imageUrl = null;
        }
    }

    // Getters and setters...
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public boolean isUnread() { return unread; }
    public boolean isImage() { return isImage; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUnread(boolean unread) { this.unread = unread; }

    // Trả về nội dung để hiển thị
    public String getDisplayContent() {
        return isImage ? "[Hình ảnh]" : content;
    }
}
