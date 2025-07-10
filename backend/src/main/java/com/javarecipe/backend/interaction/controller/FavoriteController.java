package com.javarecipe.backend.interaction.controller;

import com.javarecipe.backend.interaction.service.FavoriteService;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/recipe/{recipeId}")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isFavorited = favoriteService.toggleFavorite(currentUser.getId(), recipeId);
            long favoriteCount = favoriteService.countRecipeFavorites(recipeId);

            Map<String, Object> response = new HashMap<>();
            response.put("favorited", isFavorited);
            response.put("favoriteCount", favoriteCount);
            response.put("message", isFavorited ? "Recipe added to favorites" : "Recipe removed from favorites");

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to toggle favorite: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/recipe/{recipeId}/check")
    public ResponseEntity<?> checkFavoriteStatus(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isFavorited = favoriteService.isRecipeFavorited(currentUser.getId(), recipeId);
            long favoriteCount = favoriteService.countRecipeFavorites(recipeId);

            Map<String, Object> response = new HashMap<>();
            response.put("favorited", isFavorited);
            response.put("favoriteCount", favoriteCount);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to check favorite status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserFavorites(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<Recipe> favorites = favoriteService.getFavoriteRecipes(currentUser.getId(), pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("favorites", favorites.getContent());
            response.put("currentPage", favorites.getNumber());
            response.put("totalItems", favorites.getTotalElements());
            response.put("totalPages", favorites.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve favorites: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 