package com.example.pi_dev.model;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    public Message() {}

    public Message(Long id, Long conversationId, Long senderId,
                   String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // getters and setters
}
