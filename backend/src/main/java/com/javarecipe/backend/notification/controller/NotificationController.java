package com.javarecipe.backend.notification.controller;

import com.javarecipe.backend.notification.dto.NotificationDTO;
import com.javarecipe.backend.notification.dto.NotificationSummaryDTO;
import com.javarecipe.backend.notification.entity.Notification;
import com.javarecipe.backend.notification.service.NotificationService;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getUserNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<Notification> notifications;
            if (unreadOnly) {
                notifications = notificationService.getUnreadNotifications(currentUser.getId(), pageable);
            } else {
                notifications = notificationService.getUserNotifications(currentUser.getId(), pageable);
            }
            
            Page<NotificationDTO> notificationDTOs = notifications.map(notificationService::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDTOs.getContent());
            response.put("currentPage", notificationDTOs.getNumber());
            response.put("totalItems", notificationDTOs.getTotalElements());
            response.put("totalPages", notificationDTOs.getTotalPages());
            response.put("unreadCount", notificationService.getUnreadCount(currentUser.getId()));
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve notifications: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getNotificationSummary(@AuthenticationPrincipal User currentUser) {
        try {
            NotificationSummaryDTO summary = notificationService.getNotificationSummary(currentUser.getId());
            return ResponseEntity.ok(summary);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve notification summary: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        try {
            long unreadCount = notificationService.getUnreadCount(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve unread count: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean success = notificationService.markAsRead(notificationId, currentUser.getId());
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Notification marked as read");
                response.put("unreadCount", notificationService.getUnreadCount(currentUser.getId()));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to mark notification as read: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        try {
            int markedCount = notificationService.markAllAsRead(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            response.put("markedCount", markedCount);
            response.put("unreadCount", 0);
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to mark all notifications as read: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId(), pageable);
            
            Page<NotificationDTO> notificationDTOs = notifications.map(notificationService::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDTOs.getContent());
            response.put("currentPage", notificationDTOs.getNumber());
            response.put("totalItems", notificationDTOs.getTotalElements());
            response.put("totalPages", notificationDTOs.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve unread notifications: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
