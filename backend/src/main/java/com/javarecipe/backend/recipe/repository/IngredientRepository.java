package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.Ingredient;
import com.javarecipe.backend.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    List<Ingredient> findByRecipeOrderByDisplayOrder(Recipe recipe);
    
    void deleteByRecipe(Recipe recipe);
} 