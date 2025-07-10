package com.javarecipe.backend.interaction.repository;

import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.interaction.entity.Like;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByUserAndRecipe(User user, Recipe recipe);
    
    Optional<Like> findByUserAndComment(User user, Comment comment);
    
    boolean existsByUserAndRecipe(User user, Recipe recipe);
    
    boolean existsByUserAndComment(User user, Comment comment);
    
    long countByRecipe(Recipe recipe);
    
    long countByComment(Comment comment);
    
    void deleteByUserAndRecipe(User user, Recipe recipe);
    
    void deleteByUserAndComment(User user, Comment comment);
} 