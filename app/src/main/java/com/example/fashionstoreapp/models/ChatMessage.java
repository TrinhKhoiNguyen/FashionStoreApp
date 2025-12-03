package com.example.fashionstoreapp.models;

public class ChatMessage {
    private String messageId;
    private String content;
    private boolean isUser;
    private long timestamp;
    private String userId;

    public ChatMessage() {
        // Constructor rỗng cho Firestore
    }

    public ChatMessage(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String messageId, String content, boolean isUser, long timestamp, String userId) {
        this.messageId = messageId;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters và Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}