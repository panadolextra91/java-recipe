package com.javarecipe.backend.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    @Min(value = 0, message = "Preparation time cannot be negative")
    private Integer prepTime;
    
    @Min(value = 0, message = "Cooking time cannot be negative")
    private Integer cookTime;
    
    @Min(value = 1, message = "Servings must be at least 1")
    @Max(value = 100, message = "Servings cannot exceed 100")
    private Integer servings;
    
    private String difficulty;
    
    private boolean isPublished = false;
    
    private Set<Long> categoryIds = new HashSet<>();
    
    private Set<Long> consumerWarningIds = new HashSet<>();
    
    @Valid
    private List<IngredientRequest> ingredients = new ArrayList<>();
    
    @Valid
    private List<InstructionRequest> instructions = new ArrayList<>();
    
    @Valid
    private List<RecipeImageRequest> images = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientRequest {
        @NotBlank(message = "Ingredient name is required")
        private String name;
        
        private String quantity;
        
        private String unit;
        
        private Integer displayOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructionRequest {
        @NotBlank(message = "Instruction description is required")
        private String description;
        
        @NotNull(message = "Step number is required")
        @Min(value = 1, message = "Step number must be at least 1")
        private Integer stepNumber;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeImageRequest {
        @NotBlank(message = "Image URL is required")
        private String imageUrl;
        
        private boolean isPrimary;
        
        private Integer displayOrder;
    }
} 