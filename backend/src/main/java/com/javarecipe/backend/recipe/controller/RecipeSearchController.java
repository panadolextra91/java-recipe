package com.javarecipe.backend.recipe.controller;

import com.javarecipe.backend.recipe.dto.IngredientSearchDTO;
import com.javarecipe.backend.recipe.dto.RecipeMatchDTO;
import com.javarecipe.backend.recipe.dto.RecipeSearchRequest;
import com.javarecipe.backend.recipe.service.RecipeSearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe-search")
public class RecipeSearchController {

    private final RecipeSearchService recipeSearchService;

    @Autowired
    public RecipeSearchController(RecipeSearchService recipeSearchService) {
        this.recipeSearchService = recipeSearchService;
    }

    /**
     * Search for available ingredients
     */
    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientSearchDTO>> searchIngredients(
            @RequestParam(required = false) String query) {
        
        List<IngredientSearchDTO> ingredients;
        if (query != null && !query.trim().isEmpty()) {
            ingredients = recipeSearchService.searchIngredients(query.trim());
        } else {
            ingredients = recipeSearchService.getAllIngredients();
        }
        
        return ResponseEntity.ok(ingredients);
    }

    /**
     * Find recipes that can be made with selected ingredients
     */
    @PostMapping("/recipes")
    public ResponseEntity<?> findRecipesByIngredients(
            @Valid @RequestBody RecipeSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RecipeMatchDTO> recipes = recipeSearchService.findRecipesByIngredients(searchRequest, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recipes", recipes);
            response.put("searchCriteria", searchRequest);
            response.put("totalResults", recipes.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to search recipes: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Calculate match percentage for a specific recipe
     */
    @PostMapping("/recipes/{recipeId}/match")
    public ResponseEntity<?> calculateRecipeMatch(
            @PathVariable Long recipeId,
            @RequestBody List<String> availableIngredients) {
        
        try {
            Double matchPercentage = recipeSearchService.calculateMatchPercentage(recipeId, availableIngredients);
            List<String> missingIngredients = recipeSearchService.getMissingIngredients(recipeId, availableIngredients);
            List<String> availableRecipeIngredients = recipeSearchService.getAvailableIngredients(recipeId, availableIngredients);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recipeId", recipeId);
            response.put("matchPercentage", matchPercentage);
            response.put("missingIngredients", missingIngredients);
            response.put("availableIngredients", availableRecipeIngredients);
            response.put("totalMissingCount", missingIngredients.size());
            response.put("totalAvailableCount", availableRecipeIngredients.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to calculate recipe match: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get missing ingredients for a specific recipe
     */
    @PostMapping("/recipes/{recipeId}/missing-ingredients")
    public ResponseEntity<?> getMissingIngredients(
            @PathVariable Long recipeId,
            @RequestBody List<String> availableIngredients) {
        
        try {
            List<String> missingIngredients = recipeSearchService.getMissingIngredients(recipeId, availableIngredients);
            
            Map<String, Object> response = new HashMap<>();
            response.put("recipeId", recipeId);
            response.put("missingIngredients", missingIngredients);
            response.put("missingCount", missingIngredients.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get missing ingredients: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get example search request for documentation/testing
     */
    @GetMapping("/example")
    public ResponseEntity<RecipeSearchRequest> getExampleSearchRequest() {
        RecipeSearchRequest example = new RecipeSearchRequest();
        example.setAvailableIngredients(List.of("chicken", "rice", "onion", "garlic"));
        example.setMinMatchPercentage(50.0);
        example.setExactMatchOnly(false);
        example.setSortBy("matchPercentage");
        example.setSortDirection("desc");
        
        return ResponseEntity.ok(example);
    }
}
