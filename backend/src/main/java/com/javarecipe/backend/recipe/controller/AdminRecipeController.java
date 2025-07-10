package com.javarecipe.backend.recipe.controller;

import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/recipes")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRecipeController {

    private final RecipeService recipeService;

    @Autowired
    public AdminRecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Boolean published) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Recipe> recipePage;
        
        if (published != null) {
            recipePage = recipeService.getAllRecipesByPublishedStatus(published, pageable);
        } else {
            recipePage = recipeService.getAllRecipes(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("totalPages", recipePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishRecipe(@PathVariable Long id, @RequestParam boolean publish) {
        try {
            Recipe recipe = recipeService.setRecipePublishedStatus(id, publish);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", publish ? "Recipe published successfully" : "Recipe unpublished successfully");
            response.put("recipe", recipe);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        try {
            recipeService.deleteRecipeByAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Recipe deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
} 