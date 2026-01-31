package com.example.pi_dev.model;

import java.time.LocalDateTime;

public class Conversation {
    private Long id;
    private String type;
    private String contextType;
    private Long contextId;
    private LocalDateTime createdAt;

    public Conversation() {}

    public Conversation(Long id, String type, String contextType,
                        Long contextId, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.contextType = contextType;
        this.contextId = contextId;
        this.createdAt = createdAt;
    }

    // getters and setters
}
