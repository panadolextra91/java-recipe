package com.javarecipe.backend.interaction.controller;

import com.javarecipe.backend.interaction.service.LikeService;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/recipe/{recipeId}")
    public ResponseEntity<?> toggleRecipeLike(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isLiked = likeService.toggleRecipeLike(currentUser.getId(), recipeId);
            long likeCount = likeService.countRecipeLikes(recipeId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "Recipe liked successfully" : "Recipe unliked successfully");

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to toggle like: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<?> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isLiked = likeService.toggleCommentLike(currentUser.getId(), commentId);
            long likeCount = likeService.countCommentLikes(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "Comment liked successfully" : "Comment unliked successfully");

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to toggle like: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/recipe/{recipeId}/check")
    public ResponseEntity<?> checkRecipeLikeStatus(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isLiked = likeService.hasUserLikedRecipe(currentUser.getId(), recipeId);
            long likeCount = likeService.countRecipeLikes(recipeId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to check like status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/comment/{commentId}/check")
    public ResponseEntity<?> checkCommentLikeStatus(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {

        try {
            boolean isLiked = likeService.hasUserLikedComment(currentUser.getId(), commentId);
            long likeCount = likeService.countCommentLikes(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to check like status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 