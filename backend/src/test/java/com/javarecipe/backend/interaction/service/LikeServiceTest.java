package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.entity.Like;
import com.javarecipe.backend.interaction.repository.LikeRepository;
import com.javarecipe.backend.notification.service.NotificationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
    }

    @Test
    void testToggleRecipeLike_ShouldLikeWhenNotLiked() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(likeRepository.findByUserAndRecipe(testUser, testRecipe)).thenReturn(Optional.empty());

        // When
        boolean result = likeService.toggleRecipeLike(1L, 1L);

        // Then
        assertTrue(result, "Should return true when liking a recipe");
        verify(likeRepository).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void testToggleRecipeLike_ShouldUnlikeWhenAlreadyLiked() {
        // Given
        Like existingLike = new Like();
        existingLike.setUser(testUser);
        existingLike.setRecipe(testRecipe);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(likeRepository.findByUserAndRecipe(testUser, testRecipe)).thenReturn(Optional.of(existingLike));

        // When
        boolean result = likeService.toggleRecipeLike(1L, 1L);

        // Then
        assertFalse(result, "Should return false when unliking a recipe");
        verify(likeRepository).delete(existingLike);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void testHasUserLikedRecipe_ShouldReturnTrueWhenLiked() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(likeRepository.existsByUserAndRecipe(testUser, testRecipe)).thenReturn(true);

        // When
        boolean result = likeService.hasUserLikedRecipe(1L, 1L);

        // Then
        assertTrue(result, "Should return true when user has liked the recipe");
    }

    @Test
    void testHasUserLikedRecipe_ShouldReturnFalseWhenNotLiked() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(likeRepository.existsByUserAndRecipe(testUser, testRecipe)).thenReturn(false);

        // When
        boolean result = likeService.hasUserLikedRecipe(1L, 1L);

        // Then
        assertFalse(result, "Should return false when user has not liked the recipe");
    }

    @Test
    void testCountRecipeLikes() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(likeRepository.countByRecipe(testRecipe)).thenReturn(5L);

        // When
        long result = likeService.countRecipeLikes(1L);

        // Then
        assertEquals(5L, result, "Should return correct like count");
    }
}
