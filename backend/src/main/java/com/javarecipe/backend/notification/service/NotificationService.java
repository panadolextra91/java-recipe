package com.javarecipe.backend.notification.service;

import com.javarecipe.backend.notification.dto.NotificationDTO;
import com.javarecipe.backend.notification.dto.NotificationSummaryDTO;
import com.javarecipe.backend.notification.entity.Notification;
import com.javarecipe.backend.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    /**
     * Create a notification for a user
     * @param userId the user to notify
     * @param message the notification message
     * @param notificationType the type of notification
     * @param entityId the ID of the related entity (recipe, comment, etc.)
     * @return the created notification
     */
    Notification createNotification(Long userId, String message, NotificationType notificationType, Long entityId);
    
    /**
     * Create a notification when someone likes a recipe
     * @param recipeId the recipe that was liked
     * @param actorUserId the user who liked the recipe
     */
    void createRecipeLikeNotification(Long recipeId, Long actorUserId);
    
    /**
     * Create a notification when someone comments on a recipe
     * @param recipeId the recipe that was commented on
     * @param actorUserId the user who commented
     */
    void createRecipeCommentNotification(Long recipeId, Long actorUserId);
    
    /**
     * Create a notification when someone replies to a comment
     * @param commentId the comment that was replied to
     * @param actorUserId the user who replied
     */
    void createCommentReplyNotification(Long commentId, Long actorUserId);
    
    /**
     * Create a notification when someone likes a comment
     * @param commentId the comment that was liked
     * @param actorUserId the user who liked the comment
     */
    void createCommentLikeNotification(Long commentId, Long actorUserId);
    
    /**
     * Create a notification when someone reviews a recipe
     * @param recipeId the recipe that was reviewed
     * @param actorUserId the user who reviewed
     */
    void createRecipeReviewNotification(Long recipeId, Long actorUserId);
    
    /**
     * Get all notifications for a user with pagination
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<Notification> getUserNotifications(Long userId, Pageable pageable);
    
    /**
     * Get unread notifications for a user with pagination
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of unread notifications
     */
    Page<Notification> getUnreadNotifications(Long userId, Pageable pageable);
    
    /**
     * Mark a specific notification as read
     * @param notificationId the notification ID
     * @param userId the user ID (for authorization)
     * @return true if marked as read, false if not found or unauthorized
     */
    boolean markAsRead(Long notificationId, Long userId);
    
    /**
     * Mark all notifications as read for a user
     * @param userId the user ID
     * @return number of notifications marked as read
     */
    int markAllAsRead(Long userId);
    
    /**
     * Get notification summary for a user
     * @param userId the user ID
     * @return summary with counts
     */
    NotificationSummaryDTO getNotificationSummary(Long userId);
    
    /**
     * Get count of unread notifications for a user
     * @param userId the user ID
     * @return count of unread notifications
     */
    long getUnreadCount(Long userId);
    
    /**
     * Convert Notification entity to NotificationDTO
     * @param notification the notification entity
     * @return the notification DTO
     */
    NotificationDTO convertToDTO(Notification notification);
    
    /**
     * Delete old read notifications (cleanup)
     * @param daysOld notifications older than this many days
     * @return number of notifications deleted
     */
    int deleteOldNotifications(int daysOld);
}
