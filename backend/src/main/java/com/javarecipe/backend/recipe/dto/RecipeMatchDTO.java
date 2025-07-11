package com.javarecipe.backend.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeMatchDTO {
    private Long id;
    private String title;
    private String description;
    private Integer prepTime;
    private Integer cookTime;
    private Integer servings;
    private String difficulty;
    private Double averageRating;
    private Integer reviewCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    
    // User information
    private String authorUsername;
    private String authorDisplayName;
    
    // Primary image
    private String primaryImageUrl;
    
    // Match information
    private Double matchPercentage; // Percentage of ingredients user has
    private Integer totalIngredients; // Total ingredients in recipe
    private Integer matchedIngredients; // How many ingredients user has
    private List<String> missingIngredients; // Ingredients user doesn't have
    private List<String> availableIngredients; // Ingredients user has
    
    // Categories
    private List<String> categories;
}
