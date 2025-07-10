package com.javarecipe.backend.interaction.repository;

import com.javarecipe.backend.interaction.entity.Review;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByUserAndRecipe(User user, Recipe recipe);
    
    boolean existsByUserAndRecipe(User user, Recipe recipe);
    
    Page<Review> findByRecipe(Recipe recipe, Pageable pageable);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    long countByRecipe(Recipe recipe);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.recipe = :recipe")
    Double findAverageRatingByRecipe(@Param("recipe") Recipe recipe);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.recipe.id = :recipeId")
    Double findAverageRatingByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.recipe.id = :recipeId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> findRatingDistributionByRecipeId(@Param("recipeId") Long recipeId);

    // Admin user management methods
    long countByUser(User user);
}