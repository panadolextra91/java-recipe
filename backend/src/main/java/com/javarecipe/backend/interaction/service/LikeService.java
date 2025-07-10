package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.entity.Like;

public interface LikeService {
    
    /**
     * Toggle like on a recipe
     * @param userId User ID
     * @param recipeId Recipe ID
     * @return true if liked, false if unliked
     */
    boolean toggleRecipeLike(Long userId, Long recipeId);
    
    /**
     * Toggle like on a comment
     * @param userId User ID
     * @param commentId Comment ID
     * @return true if liked, false if unliked
     */
    boolean toggleCommentLike(Long userId, Long commentId);
    
    /**
     * Check if user has liked a recipe
     * @param userId User ID
     * @param recipeId Recipe ID
     * @return true if liked, false otherwise
     */
    boolean hasUserLikedRecipe(Long userId, Long recipeId);
    
    /**
     * Check if user has liked a comment
     * @param userId User ID
     * @param commentId Comment ID
     * @return true if liked, false otherwise
     */
    boolean hasUserLikedComment(Long userId, Long commentId);
    
    /**
     * Count likes for a recipe
     * @param recipeId Recipe ID
     * @return Number of likes
     */
    long countRecipeLikes(Long recipeId);
    
    /**
     * Count likes for a comment
     * @param commentId Comment ID
     * @return Number of likes
     */
    long countCommentLikes(Long commentId);
} 