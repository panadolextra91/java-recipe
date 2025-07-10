package com.javarecipe.backend.notification.repository;

import com.javarecipe.backend.notification.entity.Notification;
import com.javarecipe.backend.notification.entity.NotificationType;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    long countByUserAndIsReadFalse(User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.user = :user")
    int markAsRead(@Param("id") Long id, @Param("user") User user);
    
    Page<Notification> findByUserAndNotificationType(User user, NotificationType notificationType, Pageable pageable);

    // Batching methods
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.batchKey = :batchKey AND n.createdAt > :since")
    Optional<Notification> findRecentBatchedNotification(@Param("user") User user,
                                                         @Param("batchKey") String batchKey,
                                                         @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE Notification n SET n.batchCount = :batchCount, n.lastBatchUpdate = CURRENT_TIMESTAMP, n.batchActors = :batchActors, n.message = :message WHERE n.id = :id")
    int updateBatchedNotification(@Param("id") Long id,
                                 @Param("batchCount") Integer batchCount,
                                 @Param("batchActors") String batchActors,
                                 @Param("message") String message);
} 