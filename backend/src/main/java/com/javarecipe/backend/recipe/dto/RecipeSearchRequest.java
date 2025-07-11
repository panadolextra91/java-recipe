package com.javarecipe.backend.recipe.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchRequest {
    
    @NotEmpty(message = "At least one ingredient must be selected")
    private List<String> availableIngredients;
    
    @DecimalMin(value = "0.0", message = "Minimum match percentage must be at least 0%")
    @DecimalMax(value = "100.0", message = "Minimum match percentage cannot exceed 100%")
    private Double minMatchPercentage = 0.0; // Default to show all matches
    
    private Boolean exactMatchOnly = false; // Only show recipes where user has ALL ingredients
    
    private List<Long> categoryIds; // Optional category filter
    
    private String sortBy = "matchPercentage"; // matchPercentage, rating, viewCount, createdAt
    
    private String sortDirection = "desc"; // asc or desc
}
