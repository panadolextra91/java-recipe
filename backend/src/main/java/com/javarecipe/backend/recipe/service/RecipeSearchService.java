package com.javarecipe.backend.recipe.service;

import com.javarecipe.backend.recipe.dto.IngredientSearchDTO;
import com.javarecipe.backend.recipe.dto.RecipeMatchDTO;
import com.javarecipe.backend.recipe.dto.RecipeSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipeSearchService {
    
    /**
     * Search for available ingredients
     */
    List<IngredientSearchDTO> searchIngredients(String query);
    
    /**
     * Get all available ingredients
     */
    List<IngredientSearchDTO> getAllIngredients();
    
    /**
     * Find recipes that can be made with selected ingredients
     */
    Page<RecipeMatchDTO> findRecipesByIngredients(RecipeSearchRequest searchRequest, Pageable pageable);
    
    /**
     * Calculate match percentage for a recipe given available ingredients
     */
    Double calculateMatchPercentage(Long recipeId, List<String> availableIngredients);
    
    /**
     * Get missing ingredients for a recipe given available ingredients
     */
    List<String> getMissingIngredients(Long recipeId, List<String> availableIngredients);
    
    /**
     * Get available ingredients for a recipe given user's ingredients
     */
    List<String> getAvailableIngredients(Long recipeId, List<String> userIngredients);
}
