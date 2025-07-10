package com.javarecipe.backend.interaction.repository;

import com.javarecipe.backend.interaction.entity.Favorite;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    Optional<Favorite> findByUserAndRecipe(User user, Recipe recipe);
    
    boolean existsByUserAndRecipe(User user, Recipe recipe);
    
    long countByRecipe(Recipe recipe);
    
    void deleteByUserAndRecipe(User user, Recipe recipe);
    
    Page<Favorite> findByUser(User user, Pageable pageable);
    
    @Query("SELECT f.recipe FROM Favorite f WHERE f.user.id = :userId")
    Page<Recipe> findFavoriteRecipesByUserId(@Param("userId") Long userId, Pageable pageable);

    // Admin user management methods
    long countByUser(User user);
}