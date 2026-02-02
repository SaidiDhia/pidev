package com.example.pi_dev.model;

import java.time.LocalDateTime;

public class Message {

    private long id;
    private long conversationId;
    private long senderId;
    private String content;
    private LocalDateTime createdAt;

    public Message() {}

    public Message(long id, long conversationId, long senderId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }
    public Message(long conversationId, long senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getConversationId() { return conversationId; }
    public void setConversationId(long conversationId) { this.conversationId = conversationId; }

    public long getSenderId() { return senderId; }
    public void setSenderId(long senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "[" + createdAt.toLocalTime().withSecond(0) + "] User "
                + senderId + ": " + content;
    }


}
