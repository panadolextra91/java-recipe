package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.entity.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, Long> {
    
    List<RecipeImage> findByRecipeOrderByDisplayOrder(Recipe recipe);
    
    Optional<RecipeImage> findByRecipeAndIsPrimaryTrue(Recipe recipe);
    
    void deleteByRecipe(Recipe recipe);
} 