package com.example.datn_md02_admim.Model;

/**
 * Model đại diện cho một nhân viên trong danh sách chat.
 * Biến 'unread' thể hiện rằng admin có tin nhắn chưa đọc từ user này.
 * Bổ sung thêm avatar + trạng thái online để hiển thị giống bên user.
 */
public class ChatStaff {
    private String email;
    private String fullName;
    private String avatar;                 // ảnh đại diện
    private boolean online;                 // true nếu đang online
    private long lastMessageTimestamp;
    private String lastMessageText;
    private boolean unread;                 // true nếu có tin nhắn chưa đọc

    public ChatStaff() {}

    public ChatStaff(String email, String fullName, String avatar,
                     boolean online, String lastMessageText,
                     long lastMessageTimestamp, boolean unread) {
        this.email = email;
        this.fullName = fullName;
        this.avatar = avatar;
        this.online = online;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
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

    /** @return true nếu có tin nhắn chưa đọc */
    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    /** Hỗ trợ cho adapter dùng chung với user */
    public boolean isHasUnread() {
        return unread;
    }
}
