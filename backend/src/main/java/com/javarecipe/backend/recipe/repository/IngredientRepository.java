package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.Ingredient;
import com.javarecipe.backend.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByRecipeOrderByDisplayOrder(Recipe recipe);

    void deleteByRecipe(Recipe recipe);

    // Ingredient search methods for "Recipes I Can Make" feature
    @Query("SELECT DISTINCT LOWER(TRIM(i.name)) FROM Ingredient i WHERE i.recipe.isPublished = true ORDER BY LOWER(TRIM(i.name))")
    List<String> findDistinctIngredientNames();

    @Query("SELECT DISTINCT LOWER(TRIM(i.name)) FROM Ingredient i WHERE i.recipe.isPublished = true AND LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY LOWER(TRIM(i.name))")
    List<String> searchIngredientNames(@Param("query") String query);

    @Query("SELECT COUNT(DISTINCT i.recipe.id) FROM Ingredient i WHERE LOWER(TRIM(i.name)) = LOWER(:ingredientName) AND i.recipe.isPublished = true")
    Long countRecipesByIngredientName(@Param("ingredientName") String ingredientName);
}