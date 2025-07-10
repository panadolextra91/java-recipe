package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.entity.Favorite;
import com.javarecipe.backend.interaction.repository.FavoriteRepository;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
                              UserRepository userRepository,
                              RecipeRepository recipeRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndRecipe(user, recipe);
        
        if (existingFavorite.isPresent()) {
            // Remove from favorites
            favoriteRepository.delete(existingFavorite.get());
            return false;
        } else {
            // Add to favorites
            Favorite newFavorite = Favorite.builder()
                    .user(user)
                    .recipe(recipe)
                    .build();
            favoriteRepository.save(newFavorite);
            return true;
        }
    }

    @Override
    public boolean isRecipeFavorited(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return favoriteRepository.existsByUserAndRecipe(user, recipe);
    }

    @Override
    public long countRecipeFavorites(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return favoriteRepository.countByRecipe(recipe);
    }

    @Override
    public Page<Recipe> getFavoriteRecipes(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return favoriteRepository.findFavoriteRecipesByUserId(userId, pageable);
    }
} 