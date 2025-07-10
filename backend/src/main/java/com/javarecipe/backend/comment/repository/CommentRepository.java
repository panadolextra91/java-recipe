package com.javarecipe.backend.comment.repository;

import com.javarecipe.backend.comment.entity.Comment;
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
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByRecipeAndParentCommentIsNull(Recipe recipe, Pageable pageable);
    
    List<Comment> findByParentComment(Comment parentComment);
    
    Page<Comment> findByUser(User user, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.recipe.id = :recipeId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByRecipeId(@Param("recipeId") Long recipeId, Pageable pageable);

    long countByRecipe(Recipe recipe);

    // Admin user management methods
    long countByUser(User user);
}