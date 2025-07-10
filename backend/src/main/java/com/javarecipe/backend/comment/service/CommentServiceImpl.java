package com.javarecipe.backend.comment.service;

import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.comment.repository.CommentRepository;
import com.javarecipe.backend.notification.service.NotificationService;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              RecipeRepository recipeRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Comment createComment(Long recipeId, String content, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setRecipe(recipe);
        comment.setUser(user);
        comment.setLikeCount(0);

        Comment savedComment = commentRepository.save(comment);

        // Create notification for recipe owner
        notificationService.createRecipeCommentNotification(recipeId, userId);

        return savedComment;
    }

    @Override
    @Transactional
    public Comment createReply(Long parentCommentId, String content, Long userId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent comment not found with id: " + parentCommentId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Comment reply = new Comment();
        reply.setContent(content);
        reply.setRecipe(parentComment.getRecipe());
        reply.setUser(user);
        reply.setParentComment(parentComment);
        reply.setLikeCount(0);

        Comment savedReply = commentRepository.save(reply);

        // Create notification for parent comment owner
        notificationService.createCommentReplyNotification(parentCommentId, userId);

        return savedReply;
    }

    @Override
    public Page<Comment> getCommentsByRecipeId(Long recipeId, Pageable pageable) {
        return commentRepository.findTopLevelCommentsByRecipeId(recipeId, pageable);
    }

    @Override
    public Page<Comment> getRepliesByCommentId(Long commentId, Pageable pageable) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        // Get all replies for the comment and convert to page
        List<Comment> replies = commentRepository.findByParentComment(parentComment);
        
        // Convert the list to a page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), replies.size());
        
        // Check for valid sublist bounds
        if (start > replies.size()) {
            return Page.empty(pageable);
        }
        
        List<Comment> pageContent = replies.subList(start, end);
        return new PageImpl<>(pageContent, pageable, replies.size());
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String content, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        if (!isUserAuthorized(comment, userId)) {
            throw new AccessDeniedException("User not authorized to update this comment");
        }
        
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        if (!isUserAuthorized(comment, userId)) {
            throw new AccessDeniedException("User not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
    }

    @Override
    public boolean isUserAuthorized(Comment comment, Long userId) {
        // Users can modify their own comments
        return comment.getUser().getId().equals(userId) ||
               // Check if user has admin role - this would require a more complex check
               // For now, we're only allowing comment owners to modify their comments
               false;
    }
} 