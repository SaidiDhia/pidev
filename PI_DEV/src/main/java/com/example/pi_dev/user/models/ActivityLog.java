package com.example.pi_dev.user.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class ActivityLog {
    private UUID logId;
    private UUID userId;
    private String action;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(UUID logId, UUID userId, String action, LocalDateTime createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.createdAt = createdAt;
    }

    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
