package com.javarecipe.backend.recipe.controller;

import com.javarecipe.backend.common.service.CloudinaryService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling recipe-related endpoints
 * This class will automatically trigger a restart when modified thanks to DevTools
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public RecipeController(RecipeService recipeService, CloudinaryService cloudinaryService) {
        this.recipeService = recipeService;
        this.cloudinaryService = cloudinaryService;
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

    @PostMapping("/with-images")
    public ResponseEntity<?> createRecipeWithImages(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "prepTime", required = false) Integer prepTime,
            @RequestParam(value = "cookTime", required = false) Integer cookTime,
            @RequestParam(value = "servings", required = false) Integer servings,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "isPublished", defaultValue = "false") boolean isPublished,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "ingredients", required = false) String ingredientsJson,
            @RequestParam(value = "instructions", required = false) String instructionsJson,
            @AuthenticationPrincipal User currentUser) {

        try {
            // Create basic recipe request
            RecipeRequest recipeRequest = new RecipeRequest();
            recipeRequest.setTitle(title);
            recipeRequest.setDescription(description);
            recipeRequest.setPrepTime(prepTime);
            recipeRequest.setCookTime(cookTime);
            recipeRequest.setServings(servings);
            recipeRequest.setDifficulty(difficulty);
            recipeRequest.setPublished(isPublished);

            // Handle image uploads
            List<RecipeRequest.RecipeImageRequest> imageRequests = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    try {
                        CloudinaryService.CloudinaryUploadResult uploadResult =
                                cloudinaryService.uploadRecipeImage(image);

                        RecipeRequest.RecipeImageRequest imageRequest = new RecipeRequest.RecipeImageRequest();
                        imageRequest.setImageUrl(uploadResult.getUrl());
                        imageRequest.setPrimary(i == 0); // First image is primary
                        imageRequest.setDisplayOrder(i + 1);

                        imageRequests.add(imageRequest);
                    } catch (IOException e) {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Failed to upload image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
            }
            recipeRequest.setImages(imageRequests);

            // TODO: Parse ingredients and instructions JSON if provided
            // For now, we'll leave them empty - this can be enhanced later

            Recipe savedRecipe = recipeService.createRecipe(recipeRequest, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recipe created successfully with images");
            response.put("recipe", savedRecipe);
            response.put("uploadedImages", imageRequests.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

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

    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishRecipe(
            @PathVariable Long id,
            @RequestParam boolean publish,
            @AuthenticationPrincipal User currentUser) {
        try {
            Recipe recipe = recipeService.setUserRecipePublishedStatus(id, publish, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", publish ? "Recipe published successfully" : "Recipe unpublished successfully");
            response.put("recipe", recipe);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Recipe not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (AccessDeniedException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You are not authorized to publish/unpublish this recipe");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update recipe publication status: " + e.getMessage());
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