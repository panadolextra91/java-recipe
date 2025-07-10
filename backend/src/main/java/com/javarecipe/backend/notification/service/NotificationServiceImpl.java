package com.javarecipe.backend.notification.service;

import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.comment.repository.CommentRepository;
import com.javarecipe.backend.notification.dto.NotificationDTO;
import com.javarecipe.backend.notification.dto.NotificationSummaryDTO;
import com.javarecipe.backend.notification.entity.Notification;
import com.javarecipe.backend.notification.entity.NotificationType;
import com.javarecipe.backend.notification.repository.NotificationRepository;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   RecipeRepository recipeRepository,
                                   CommentRepository commentRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String message, NotificationType notificationType, Long entityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setEntityId(entityId);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createRecipeLikeNotification(Long recipeId, Long actorUserId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorUserId));

        // Don't notify if user likes their own recipe
        if (recipe.getUser().getId().equals(actorUserId)) {
            return;
        }

        // Try to batch with existing like notification
        createOrUpdateBatchedLikeNotification(recipe, actor);
    }

    @Override
    @Transactional
    public void createRecipeCommentNotification(Long recipeId, Long actorUserId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorUserId));

        // Don't notify if user comments on their own recipe
        if (recipe.getUser().getId().equals(actorUserId)) {
            return;
        }

        String message = String.format("%s commented on your recipe \"%s\"", 
                actor.getUsername(), recipe.getTitle());

        createNotification(recipe.getUser().getId(), message, NotificationType.RECIPE_COMMENT, recipeId);
    }

    @Override
    @Transactional
    public void createCommentReplyNotification(Long commentId, Long actorUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorUserId));

        // Don't notify if user replies to their own comment
        if (comment.getUser().getId().equals(actorUserId)) {
            return;
        }

        String message = String.format("%s replied to your comment on \"%s\"", 
                actor.getUsername(), comment.getRecipe().getTitle());

        createNotification(comment.getUser().getId(), message, NotificationType.COMMENT_REPLY, commentId);
    }

    @Override
    @Transactional
    public void createCommentLikeNotification(Long commentId, Long actorUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorUserId));

        // Don't notify if user likes their own comment
        if (comment.getUser().getId().equals(actorUserId)) {
            return;
        }

        String message = String.format("%s liked your comment on \"%s\"", 
                actor.getUsername(), comment.getRecipe().getTitle());

        createNotification(comment.getUser().getId(), message, NotificationType.COMMENT_LIKE, commentId);
    }

    @Override
    @Transactional
    public void createRecipeReviewNotification(Long recipeId, Long actorUserId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorUserId));

        // Don't notify if user reviews their own recipe
        if (recipe.getUser().getId().equals(actorUserId)) {
            return;
        }

        String message = String.format("%s reviewed your recipe \"%s\"", 
                actor.getUsername(), recipe.getTitle());

        createNotification(recipe.getUser().getId(), message, NotificationType.RECIPE_REVIEW, recipeId);
    }

    @Override
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public Page<Notification> getUnreadNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        int updated = notificationRepository.markAsRead(notificationId, user);
        return updated > 0;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return notificationRepository.markAllAsRead(user);
    }

    @Override
    public NotificationSummaryDTO getNotificationSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        // For total count, we could add a method to count all notifications
        // For now, let's use a simple approach
        long totalCount = unreadCount; // This could be enhanced to get actual total

        NotificationSummaryDTO summary = new NotificationSummaryDTO();
        summary.setUnreadCount(unreadCount);
        summary.setTotalNotifications(totalCount);
        summary.setReadCount(totalCount - unreadCount);
        return summary;
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setNotificationType(notification.getNotificationType());
        dto.setEntityId(notification.getEntityId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());

        // Add batching information
        dto.setBatchCount(notification.getBatchCount());
        dto.setLastBatchUpdate(notification.getLastBatchUpdate());
        if (notification.getBatchActors() != null) {
            dto.setBatchActors(deserializeActors(notification.getBatchActors()));
        }

        // Add context information based on notification type
        addContextInformation(dto, notification);

        return dto;
    }

    private void addContextInformation(NotificationDTO dto, Notification notification) {
        try {
            switch (notification.getNotificationType()) {
                case RECIPE_LIKE:
                case RECIPE_COMMENT:
                case RECIPE_REVIEW:
                    Recipe recipe = recipeRepository.findById(notification.getEntityId()).orElse(null);
                    if (recipe != null) {
                        dto.setEntityTitle(recipe.getTitle());
                        dto.setEntityUrl("/recipes/" + recipe.getId());
                    }
                    break;
                case COMMENT_REPLY:
                case COMMENT_LIKE:
                    Comment comment = commentRepository.findById(notification.getEntityId()).orElse(null);
                    if (comment != null) {
                        dto.setEntityTitle(comment.getRecipe().getTitle());
                        dto.setEntityUrl("/recipes/" + comment.getRecipe().getId() + "#comment-" + comment.getId());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // If we can't load context information, just continue without it
        }
    }

    @Override
    @Transactional
    public int deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        // This would require a custom query in the repository
        // For now, return 0 as placeholder
        return 0;
    }

    // Batching helper methods
    private void createOrUpdateBatchedLikeNotification(Recipe recipe, User actor) {
        String batchKey = createBatchKey(NotificationType.RECIPE_LIKE, recipe.getId());
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Look for existing notification in the last hour
        Optional<Notification> existingNotification = notificationRepository
                .findRecentBatchedNotification(recipe.getUser(), batchKey, oneHourAgo);

        if (existingNotification.isPresent()) {
            // Update existing batched notification
            updateBatchedNotification(existingNotification.get(), actor, recipe.getTitle());
        } else {
            // Create new notification
            createNewBatchedLikeNotification(recipe, actor, batchKey);
        }
    }

    private void createNewBatchedLikeNotification(Recipe recipe, User actor, String batchKey) {
        String message = String.format("%s liked your recipe \"%s\"",
                actor.getUsername(), recipe.getTitle());

        Notification notification = new Notification();
        notification.setUser(recipe.getUser());
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.RECIPE_LIKE);
        notification.setEntityId(recipe.getId());
        notification.setRead(false);
        notification.setBatchKey(batchKey);
        notification.setBatchCount(1);
        notification.setLastBatchUpdate(LocalDateTime.now());
        notification.setBatchActors(serializeActors(Arrays.asList(actor.getUsername())));

        notificationRepository.save(notification);
    }

    private void updateBatchedNotification(Notification notification, User actor, String entityTitle) {
        List<String> actors = deserializeActors(notification.getBatchActors());

        // Add new actor if not already in the list
        if (!actors.contains(actor.getUsername())) {
            actors.add(actor.getUsername());
        }

        int newCount = actors.size();
        String newMessage = createBatchedMessage(actors, entityTitle, notification.getNotificationType());

        notificationRepository.updateBatchedNotification(
                notification.getId(),
                newCount,
                serializeActors(actors),
                newMessage
        );
    }

    private String createBatchKey(NotificationType type, Long entityId) {
        return type.name() + "_" + entityId;
    }

    private String createBatchedMessage(List<String> actors, String entityTitle, NotificationType type) {
        if (actors.size() == 1) {
            return String.format("%s liked your recipe \"%s\"", actors.get(0), entityTitle);
        } else if (actors.size() == 2) {
            return String.format("%s and %s liked your recipe \"%s\"",
                    actors.get(0), actors.get(1), entityTitle);
        } else {
            return String.format("%s and %d others liked your recipe \"%s\"",
                    actors.get(0), actors.size() - 1, entityTitle);
        }
    }

    private String serializeActors(List<String> actors) {
        // Simple comma-separated serialization
        return String.join(",", actors);
    }

    private List<String> deserializeActors(String actorsString) {
        if (actorsString == null || actorsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(actorsString.split(",")));
    }
}
