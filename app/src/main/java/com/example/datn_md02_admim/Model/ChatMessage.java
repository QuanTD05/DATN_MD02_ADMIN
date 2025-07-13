package com.example.datn_md02_admim.Model;

import androidx.annotation.NonNull;

public class ChatMessage {
    private String sender;
    private String receiver;
    private String content;  // Dữ liệu mới
    private String message;  // Dữ liệu cũ
    private long timestamp;

    private boolean isUnread = true; // ✅ Mặc định là chưa đọc

    // Bắt buộc cho Firebase
    public ChatMessage() {}

    // Constructor dùng khi gửi message mới
    public ChatMessage(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isUnread = true; // Mặc định tin nhắn mới là chưa đọc
    }

    // Constructor đầy đủ (bao gồm cả message cũ & isUnread)
    public ChatMessage(String sender, String receiver, String content, String message, long timestamp, boolean isUnread) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.message = message;
        this.timestamp = timestamp;
        this.isUnread = isUnread;
    }

    // -------------------------
    // Getter & Setter
    // -------------------------

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    /**
     * ✅ Ưu tiên content mới, fallback sang message cũ nếu không có content
     */
    public String getDisplayContent() {
        return (content != null && !content.trim().isEmpty()) ? content :
                (message != null && !message.trim().isEmpty()) ? message : "";
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", isUnread=" + isUnread +
                '}';
    }
}
