package com.javarecipe.backend.interaction.controller;

import com.javarecipe.backend.interaction.dto.ReviewCreateDTO;
import com.javarecipe.backend.interaction.dto.ReviewDTO;
import com.javarecipe.backend.interaction.dto.ReviewSummaryDTO;
import com.javarecipe.backend.interaction.dto.ReviewUpdateDTO;
import com.javarecipe.backend.interaction.entity.Review;
import com.javarecipe.backend.interaction.service.ReviewService;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewCreateDTO reviewCreateDTO,
            @AuthenticationPrincipal User currentUser) {

        try {
            Review review = reviewService.createReview(
                    reviewCreateDTO.getRecipeId(),
                    reviewCreateDTO.getRating(),
                    reviewCreateDTO.getComment(),
                    currentUser.getId());

            ReviewDTO reviewDTO = reviewService.convertToDTO(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewDTO);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to create review: " + e.getMessage())
            );
        }
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> getReviewsByRecipe(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<Review> reviews = reviewService.getReviewsByRecipeId(recipeId, pageable);
            
            Page<ReviewDTO> reviewDTOs = reviews.map(reviewService::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviewDTOs.getContent());
            response.put("currentPage", reviewDTOs.getNumber());
            response.put("totalItems", reviewDTOs.getTotalElements());
            response.put("totalPages", reviewDTOs.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve reviews: " + e.getMessage())
            );
        }
    }

    @GetMapping("/recipe/{recipeId}/summary")
    public ResponseEntity<?> getReviewSummary(@PathVariable Long recipeId) {
        try {
            ReviewSummaryDTO summary = reviewService.getReviewSummary(recipeId);
            return ResponseEntity.ok(summary);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve review summary: " + e.getMessage())
            );
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Review> reviews = reviewService.getReviewsByUserId(userId, pageable);
            
            Page<ReviewDTO> reviewDTOs = reviews.map(reviewService::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviewDTOs.getContent());
            response.put("currentPage", reviewDTOs.getNumber());
            response.put("totalItems", reviewDTOs.getTotalElements());
            response.put("totalPages", reviewDTOs.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve user reviews: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            ReviewDTO reviewDTO = reviewService.convertToDTO(review);
            return ResponseEntity.ok(reviewDTO);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve review: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateDTO reviewUpdateDTO,
            @AuthenticationPrincipal User currentUser) {

        try {
            Review updatedReview = reviewService.updateReview(
                    reviewId,
                    reviewUpdateDTO.getRating(),
                    reviewUpdateDTO.getComment(),
                    currentUser.getId());

            ReviewDTO reviewDTO = reviewService.convertToDTO(updatedReview);
            return ResponseEntity.ok(reviewDTO);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to update review: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User currentUser) {

        try {
            reviewService.deleteReview(reviewId, currentUser.getId());
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to delete review: " + e.getMessage())
            );
        }
    }

    @GetMapping("/user/{userId}/recipe/{recipeId}")
    public ResponseEntity<?> getUserReviewForRecipe(
            @PathVariable Long userId,
            @PathVariable Long recipeId) {

        try {
            Review review = reviewService.getUserReviewForRecipe(userId, recipeId);
            
            if (review != null) {
                ReviewDTO reviewDTO = reviewService.convertToDTO(review);
                return ResponseEntity.ok(reviewDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("message", "User has not reviewed this recipe")
                );
            }

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve user review: " + e.getMessage())
            );
        }
    }

    @GetMapping("/user/{userId}/recipe/{recipeId}/exists")
    public ResponseEntity<?> checkUserReviewExists(
            @PathVariable Long userId,
            @PathVariable Long recipeId) {

        try {
            boolean hasReviewed = reviewService.hasUserReviewedRecipe(userId, recipeId);
            return ResponseEntity.ok(Map.of("hasReviewed", hasReviewed));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to check review status: " + e.getMessage())
            );
        }
    }
}
