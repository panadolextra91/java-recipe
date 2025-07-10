package com.javarecipe.backend.recipe.service;

import com.javarecipe.backend.recipe.dto.RecipeRequest;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecipeService {

    /**
     * Get all published recipes with pagination
     */
    Page<Recipe> getAllPublishedRecipes(Pageable pageable);
    
    /**
     * Search recipes by title or description
     */
    Page<Recipe> searchRecipes(String query, Pageable pageable);
    
    /**
     * Get recipe by ID if published
     */
    Recipe getRecipeById(Long id);
    
    /**
     * Create a new recipe
     */
    Recipe createRecipe(RecipeRequest recipeRequest, User user);
    
    /**
     * Update an existing recipe
     */
    Recipe updateRecipe(Long id, RecipeRequest recipeRequest, User user);
    
    /**
     * Delete a recipe
     */
    void deleteRecipe(Long id, User user);
    
    /**
     * Get all recipes by a specific user
     */
    Page<Recipe> getRecipesByUser(User user, Pageable pageable);
    
    /**
     * Get recipes by category name
     */
    Page<Recipe> getRecipesByCategory(String categoryName, Pageable pageable);
    
    /**
     * Check if user is authorized to modify the recipe
     */
    boolean isUserAuthorized(Recipe recipe, User user);
    
    /**
     * Get all recipes (admin only)
     */
    Page<Recipe> getAllRecipes(Pageable pageable);
    
    /**
     * Get recipes by published status (admin only)
     */
    Page<Recipe> getAllRecipesByPublishedStatus(boolean published, Pageable pageable);
    
    /**
     * Set recipe published status (admin only)
     */
    Recipe setRecipePublishedStatus(Long id, boolean publish);
    
    /**
     * Delete any recipe by ID (admin only)
     */
    void deleteRecipeByAdmin(Long id);
} 