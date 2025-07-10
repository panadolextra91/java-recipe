package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.entity.Favorite;
import com.javarecipe.backend.interaction.repository.FavoriteRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

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
    void testToggleFavorite_ShouldAddToFavoritesWhenNotFavorited() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(favoriteRepository.findByUserAndRecipe(testUser, testRecipe)).thenReturn(Optional.empty());

        // When
        boolean result = favoriteService.toggleFavorite(1L, 1L);

        // Then
        assertTrue(result, "Should return true when adding to favorites");
        verify(favoriteRepository).save(any(Favorite.class));
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }

    @Test
    void testToggleFavorite_ShouldRemoveFromFavoritesWhenAlreadyFavorited() {
        // Given
        Favorite existingFavorite = new Favorite();
        existingFavorite.setUser(testUser);
        existingFavorite.setRecipe(testRecipe);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(favoriteRepository.findByUserAndRecipe(testUser, testRecipe)).thenReturn(Optional.of(existingFavorite));

        // When
        boolean result = favoriteService.toggleFavorite(1L, 1L);

        // Then
        assertFalse(result, "Should return false when removing from favorites");
        verify(favoriteRepository).delete(existingFavorite);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void testIsRecipeFavorited_ShouldReturnTrueWhenFavorited() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(favoriteRepository.existsByUserAndRecipe(testUser, testRecipe)).thenReturn(true);

        // When
        boolean result = favoriteService.isRecipeFavorited(1L, 1L);

        // Then
        assertTrue(result, "Should return true when recipe is favorited");
    }

    @Test
    void testIsRecipeFavorited_ShouldReturnFalseWhenNotFavorited() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(favoriteRepository.existsByUserAndRecipe(testUser, testRecipe)).thenReturn(false);

        // When
        boolean result = favoriteService.isRecipeFavorited(1L, 1L);

        // Then
        assertFalse(result, "Should return false when recipe is not favorited");
    }

    @Test
    void testCountRecipeFavorites() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(favoriteRepository.countByRecipe(testRecipe)).thenReturn(3L);

        // When
        long result = favoriteService.countRecipeFavorites(1L);

        // Then
        assertEquals(3L, result, "Should return correct favorite count");
    }

    @Test
    void testGetFavoriteRecipes() {
        // Given
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setTitle("Recipe 1");
        
        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setTitle("Recipe 2");

        Page<Recipe> expectedPage = new PageImpl<>(Arrays.asList(recipe1, recipe2));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findFavoriteRecipesByUserId(1L, pageable)).thenReturn(expectedPage);

        // When
        Page<Recipe> result = favoriteService.getFavoriteRecipes(1L, pageable);

        // Then
        assertEquals(2, result.getContent().size(), "Should return correct number of favorite recipes");
        assertEquals("Recipe 1", result.getContent().get(0).getTitle());
        assertEquals("Recipe 2", result.getContent().get(1).getTitle());
    }
}
