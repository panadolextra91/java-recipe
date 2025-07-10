package com.javarecipe.backend.notification.service;

import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.comment.repository.CommentRepository;
import com.javarecipe.backend.notification.entity.Notification;
import com.javarecipe.backend.notification.entity.NotificationType;
import com.javarecipe.backend.notification.repository.NotificationRepository;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User recipeOwner;
    private User actor;
    private Recipe testRecipe;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        recipeOwner = new User();
        recipeOwner.setId(1L);
        recipeOwner.setUsername("recipeowner");

        actor = new User();
        actor.setId(2L);
        actor.setUsername("actor");

        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setUser(recipeOwner);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test comment");
        testComment.setUser(recipeOwner);
        testComment.setRecipe(testRecipe);
    }

    @Test
    void testCreateRecipeLikeNotification_ShouldCreateNotification() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(notificationRepository.findRecentBatchedNotification(any(), anyString(), any())).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createRecipeLikeNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateRecipeLikeNotification_ShouldNotCreateNotificationForOwnRecipe() {
        // Given - actor is the same as recipe owner
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner));

        // When
        notificationService.createRecipeLikeNotification(1L, 1L);

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testCreateRecipeCommentNotification_ShouldCreateNotification() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner)); // For notification recipient
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createRecipeCommentNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateCommentReplyNotification_ShouldCreateNotification() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner)); // For notification recipient
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createCommentReplyNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateCommentLikeNotification_ShouldCreateNotification() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner)); // For notification recipient
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createCommentLikeNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateRecipeReviewNotification_ShouldCreateNotification() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner)); // For notification recipient
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createRecipeReviewNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetUnreadCount() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner));
        when(notificationRepository.countByUserAndIsReadFalse(recipeOwner)).thenReturn(5L);

        // When
        long unreadCount = notificationService.getUnreadCount(1L);

        // Then
        assertEquals(5L, unreadCount);
    }

    @Test
    void testMarkAsRead() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner));
        when(notificationRepository.markAsRead(1L, recipeOwner)).thenReturn(1);

        // When
        boolean result = notificationService.markAsRead(1L, 1L);

        // Then
        assertTrue(result);
        verify(notificationRepository).markAsRead(1L, recipeOwner);
    }

    @Test
    void testMarkAllAsRead() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner));
        when(notificationRepository.markAllAsRead(recipeOwner)).thenReturn(3);

        // When
        int markedCount = notificationService.markAllAsRead(1L);

        // Then
        assertEquals(3, markedCount);
        verify(notificationRepository).markAllAsRead(recipeOwner);
    }
}
