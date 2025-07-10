package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    Page<Recipe> findAllByIsPublishedTrue(Pageable pageable);
    
    Page<Recipe> findAllByIsPublishedFalse(Pageable pageable);
    
    Page<Recipe> findAllByUser(User user, Pageable pageable);
    
    @Query("SELECT r FROM Recipe r WHERE r.isPublished = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Recipe> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT r FROM Recipe r JOIN r.categories c WHERE c.id IN :categoryIds AND r.isPublished = true")
    Page<Recipe> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredient, '%')) AND r.isPublished = true")
    Page<Recipe> findByIngredient(@Param("ingredient") String ingredient, Pageable pageable);
    
    @Query(value = "SELECT r.* FROM recipes r " +
                  "JOIN recipe_categories rc ON r.id = rc.recipe_id " +
                  "JOIN categories c ON rc.category_id = c.id " +
                  "WHERE LOWER(c.name) = LOWER(:categoryName) " +
                  "AND r.is_published = true", 
           nativeQuery = true)
    Page<Recipe> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
} 