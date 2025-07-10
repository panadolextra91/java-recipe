package com.javarecipe.backend.notification.dto;

import com.javarecipe.backend.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String message;
    private NotificationType notificationType;
    private Long entityId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Additional context fields for better frontend experience
    private String actorUsername; // Who performed the action
    private String actorAvatarUrl; // Actor's avatar
    private String entityTitle; // Recipe title or comment content preview
    private String entityUrl; // Frontend URL to navigate to

    // Batching fields
    private Integer batchCount;
    private LocalDateTime lastBatchUpdate;
    private java.util.List<String> batchActors; // List of usernames who performed the action
    
    // Explicit setters for IDE compatibility
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }
    
    public void setActorAvatarUrl(String actorAvatarUrl) {
        this.actorAvatarUrl = actorAvatarUrl;
    }
    
    public void setEntityTitle(String entityTitle) {
        this.entityTitle = entityTitle;
    }
    
    public void setEntityUrl(String entityUrl) {
        this.entityUrl = entityUrl;
    }

    public void setBatchCount(Integer batchCount) {
        this.batchCount = batchCount;
    }

    public void setLastBatchUpdate(LocalDateTime lastBatchUpdate) {
        this.lastBatchUpdate = lastBatchUpdate;
    }

    public void setBatchActors(java.util.List<String> batchActors) {
        this.batchActors = batchActors;
    }
}
