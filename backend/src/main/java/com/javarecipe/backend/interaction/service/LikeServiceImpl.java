package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.comment.repository.CommentRepository;
import com.javarecipe.backend.interaction.entity.Like;
import com.javarecipe.backend.interaction.repository.LikeRepository;
import com.javarecipe.backend.notification.service.NotificationService;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Autowired
    public LikeServiceImpl(LikeRepository likeRepository,
                           UserRepository userRepository,
                           RecipeRepository recipeRepository,
                           CommentRepository commentRepository,
                           NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public boolean toggleRecipeLike(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        Optional<Like> existingLike = likeRepository.findByUserAndRecipe(user, recipe);
        
        if (existingLike.isPresent()) {
            // Unlike - remove the like
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            // Like - create new like
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setRecipe(recipe);
            likeRepository.save(newLike);

            // Create notification for recipe owner
            notificationService.createRecipeLikeNotification(recipeId, userId);

            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        Optional<Like> existingLike = likeRepository.findByUserAndComment(user, comment);
        
        if (existingLike.isPresent()) {
            // Unlike - remove the like
            likeRepository.delete(existingLike.get());
            
            // Update comment like count
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentRepository.save(comment);
            
            return false;
        } else {
            // Like - create new like
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setComment(comment);
            likeRepository.save(newLike);

            // Update comment like count
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.save(comment);

            // Create notification for comment owner
            notificationService.createCommentLikeNotification(commentId, userId);

            return true;
        }
    }

    @Override
    public boolean hasUserLikedRecipe(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return likeRepository.existsByUserAndRecipe(user, recipe);
    }

    @Override
    public boolean hasUserLikedComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        return likeRepository.existsByUserAndComment(user, comment);
    }

    @Override
    public long countRecipeLikes(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return likeRepository.countByRecipe(recipe);
    }

    @Override
    public long countCommentLikes(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
        
        return likeRepository.countByComment(comment);
    }
} 