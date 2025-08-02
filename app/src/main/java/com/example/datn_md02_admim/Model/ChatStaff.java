package com.example.datn_md02_admim.Model;

/**
 * Model đại diện cho một nhân viên trong danh sách chat.
 * Biến 'unread' thể hiện rằng người admin có tin nhắn chưa đọc từ user này.
 */
public class ChatStaff {
    private String email;
    private String fullName;
    private long lastMessageTimestamp;
    private String lastMessageText;
    private boolean unread; // true nếu có tin nhắn chưa đọc

    // Constructor mặc định cho Firebase
    public ChatStaff() {}

    public ChatStaff(String email, String fullName, String lastMessageText, long lastMessageTimestamp, boolean unread) {
        this.email = email;
        this.fullName = fullName;
        this.lastMessageText = lastMessageText;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unread = unread;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    /**
     * @return true nếu có tin nhắn chưa đọc từ người dùng này
     */
    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
