package com.example.pi_dev.messaging.messagingmodel;

import java.time.LocalDateTime;

public class Conversation {

    private long id;
    private String type;          // PERSONAL / GROUP
    private String contextType;   // EVENT / POST / STORE
    private long contextId;
    private LocalDateTime createdAt;

    public Conversation() {}

    public Conversation(long id, String type, String contextType, long contextId, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.contextType = contextType;
        this.contextId = contextId;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }

    public long getContextId() { return contextId; }
    public void setContextId(long contextId) { this.contextId = contextId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return type + " - " + contextType + " #" + contextId;
    }


}
