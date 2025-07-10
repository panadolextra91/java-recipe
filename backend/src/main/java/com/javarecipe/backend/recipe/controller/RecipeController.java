package com.javarecipe.backend.recipe.controller;

import com.javarecipe.backend.recipe.dto.RecipeRequest;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.service.RecipeService;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling recipe-related endpoints
 * This class will automatically trigger a restart when modified thanks to DevTools
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    
    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Recipe> recipePage = recipeService.getAllPublishedRecipes(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("totalPages", recipePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRecipes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage = recipeService.searchRecipes(query, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("totalPages", recipePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-devtools")
    public ResponseEntity<Map<String, String>> testDevTools() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "DevTools automatic restart is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recipe not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createRecipe(
            @Valid @RequestBody RecipeRequest recipeRequest, 
            @AuthenticationPrincipal User currentUser) {
        
        try {
            Recipe savedRecipe = recipeService.createRecipe(recipeRequest, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create recipe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(
            @PathVariable Long id, 
            @Valid @RequestBody RecipeRequest recipeRequest, 
            @AuthenticationPrincipal User currentUser) {
        
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipeRequest, currentUser);
            return ResponseEntity.ok(updatedRecipe);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recipe not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (AccessDeniedException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You are not authorized to update this recipe");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update recipe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(
            @PathVariable Long id, 
            @AuthenticationPrincipal User currentUser) {
        
        try {
            recipeService.deleteRecipe(id, currentUser);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recipe deleted successfully");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recipe not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (AccessDeniedException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You are not authorized to delete this recipe");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete recipe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserRecipes(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Recipe> recipePage = recipeService.getRecipesByUser(currentUser, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("totalPages", recipePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Map<String, Object>> getRecipesByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage = recipeService.getRecipesByCategory(categoryName, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("totalPages", recipePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
} 