package com.javarecipe.backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryDTO {
    private long totalNotifications;
    private long unreadCount;
    private long readCount;
    
    // Explicit setters for IDE compatibility
    public void setTotalNotifications(long totalNotifications) {
        this.totalNotifications = totalNotifications;
    }
    
    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public void setReadCount(long readCount) {
        this.readCount = readCount;
    }
}
