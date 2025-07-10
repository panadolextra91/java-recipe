package com.javarecipe.backend.notification.service;

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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationBatchingTest {

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
    private User actor1;
    private User actor2;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        recipeOwner = new User();
        recipeOwner.setId(1L);
        recipeOwner.setUsername("recipeowner");

        actor1 = new User();
        actor1.setId(2L);
        actor1.setUsername("actor1");

        actor2 = new User();
        actor2.setId(3L);
        actor2.setUsername("actor2");

        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setUser(recipeOwner);
    }

    @Test
    void testCreateFirstLikeNotification_ShouldCreateNewNotification() {
        // Given - No existing notification
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor1));
        when(notificationRepository.findRecentBatchedNotification(
                eq(recipeOwner), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createRecipeLikeNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(argThat(notification -> 
                notification.getBatchCount() == 1 &&
                notification.getBatchKey().equals("RECIPE_LIKE_1") &&
                notification.getBatchActors().equals("actor1")
        ));
        verify(notificationRepository, never()).updateBatchedNotification(anyLong(), anyInt(), anyString(), anyString());
    }

    @Test
    void testCreateSecondLikeNotification_ShouldUpdateExistingNotification() {
        // Given - Existing notification from actor1
        Notification existingNotification = new Notification();
        existingNotification.setId(100L);
        existingNotification.setBatchCount(1);
        existingNotification.setBatchActors("actor1");
        existingNotification.setNotificationType(NotificationType.RECIPE_LIKE);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(3L)).thenReturn(Optional.of(actor2));
        when(notificationRepository.findRecentBatchedNotification(
                eq(recipeOwner), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingNotification));

        // When
        notificationService.createRecipeLikeNotification(1L, 3L);

        // Then
        verify(notificationRepository).updateBatchedNotification(
                eq(100L), 
                eq(2), 
                eq("actor1,actor2"), 
                eq("actor1 and actor2 liked your recipe \"Test Recipe\"")
        );
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testCreateThirdLikeNotification_ShouldShowOthersCount() {
        // Given - Existing notification from actor1 and actor2
        Notification existingNotification = new Notification();
        existingNotification.setId(100L);
        existingNotification.setBatchCount(2);
        existingNotification.setBatchActors("actor1,actor2");
        existingNotification.setNotificationType(NotificationType.RECIPE_LIKE);

        User actor3 = new User();
        actor3.setId(4L);
        actor3.setUsername("actor3");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(4L)).thenReturn(Optional.of(actor3));
        when(notificationRepository.findRecentBatchedNotification(
                eq(recipeOwner), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingNotification));

        // When
        notificationService.createRecipeLikeNotification(1L, 4L);

        // Then
        verify(notificationRepository).updateBatchedNotification(
                eq(100L), 
                eq(3), 
                eq("actor1,actor2,actor3"), 
                eq("actor1 and 2 others liked your recipe \"Test Recipe\"")
        );
    }

    @Test
    void testDuplicateLikeNotification_ShouldNotIncrementCount() {
        // Given - Existing notification already includes actor1
        Notification existingNotification = new Notification();
        existingNotification.setId(100L);
        existingNotification.setBatchCount(1);
        existingNotification.setBatchActors("actor1");
        existingNotification.setNotificationType(NotificationType.RECIPE_LIKE);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor1));
        when(notificationRepository.findRecentBatchedNotification(
                eq(recipeOwner), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingNotification));

        // When
        notificationService.createRecipeLikeNotification(1L, 2L);

        // Then - Should update with same count and actors
        verify(notificationRepository).updateBatchedNotification(
                eq(100L), 
                eq(1), 
                eq("actor1"), 
                eq("actor1 liked your recipe \"Test Recipe\"")
        );
    }

    @Test
    void testOldNotification_ShouldCreateNewNotification() {
        // Given - No recent notification (older than 1 hour)
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actor1));
        when(notificationRepository.findRecentBatchedNotification(
                eq(recipeOwner), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty()); // No recent notification found
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.createRecipeLikeNotification(1L, 2L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository, never()).updateBatchedNotification(anyLong(), anyInt(), anyString(), anyString());
    }

    @Test
    void testSelfLike_ShouldNotCreateNotification() {
        // Given - User likes their own recipe
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipeOwner));

        // When
        notificationService.createRecipeLikeNotification(1L, 1L);

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).updateBatchedNotification(anyLong(), anyInt(), anyString(), anyString());
        verify(notificationRepository, never()).findRecentBatchedNotification(any(), anyString(), any());
    }
}
