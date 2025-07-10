package com.javarecipe.backend.comment.service;

import com.javarecipe.backend.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    
    // Create a new top-level comment
    Comment createComment(Long recipeId, String content, Long userId);
    
    // Create a reply to an existing comment
    Comment createReply(Long parentCommentId, String content, Long userId);
    
    // Get all top-level comments for a recipe with pagination
    Page<Comment> getCommentsByRecipeId(Long recipeId, Pageable pageable);
    
    // Get all replies for a comment
    Page<Comment> getRepliesByCommentId(Long commentId, Pageable pageable);
    
    // Update a comment
    Comment updateComment(Long commentId, String content, Long userId);
    
    // Delete a comment (and all its replies)
    void deleteComment(Long commentId, Long userId);
    
    // Get a comment by ID
    Comment getCommentById(Long commentId);
    
    // Check if user is authorized to modify a comment
    boolean isUserAuthorized(Comment comment, Long userId);
} 