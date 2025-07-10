package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    
    /**
     * Toggle favorite status of a recipe for a user
     * @param userId User ID
     * @param recipeId Recipe ID
     * @return true if added to favorites, false if removed
     */
    boolean toggleFavorite(Long userId, Long recipeId);
    
    /**
     * Check if a recipe is in user's favorites
     * @param userId User ID
     * @param recipeId Recipe ID
     * @return true if in favorites, false otherwise
     */
    boolean isRecipeFavorited(Long userId, Long recipeId);
    
    /**
     * Count how many users have favorited a recipe
     * @param recipeId Recipe ID
     * @return Number of users who favorited the recipe
     */
    long countRecipeFavorites(Long recipeId);
    
    /**
     * Get all favorite recipes for a user with pagination
     * @param userId User ID
     * @param pageable Pagination information
     * @return Page of Recipe objects
     */
    Page<Recipe> getFavoriteRecipes(Long userId, Pageable pageable);
} 