package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.dto.ReviewDTO;
import com.javarecipe.backend.interaction.dto.ReviewSummaryDTO;
import com.javarecipe.backend.interaction.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    /**
     * Create a new review for a recipe
     * @param recipeId the recipe to review
     * @param rating the rating (1-5)
     * @param comment optional comment text
     * @param userId the user creating the review
     * @return the created review
     * @throws IllegalArgumentException if user already reviewed this recipe
     */
    Review createReview(Long recipeId, Integer rating, String comment, Long userId);
    
    /**
     * Update an existing review
     * @param reviewId the review to update
     * @param rating the new rating (1-5)
     * @param comment the new comment text
     * @param userId the user updating the review
     * @return the updated review
     * @throws IllegalArgumentException if user is not authorized
     */
    Review updateReview(Long reviewId, Integer rating, String comment, Long userId);
    
    /**
     * Delete a review
     * @param reviewId the review to delete
     * @param userId the user deleting the review
     * @throws IllegalArgumentException if user is not authorized
     */
    void deleteReview(Long reviewId, Long userId);
    
    /**
     * Get all reviews for a recipe with pagination
     * @param recipeId the recipe ID
     * @param pageable pagination parameters
     * @return page of reviews
     */
    Page<Review> getReviewsByRecipeId(Long recipeId, Pageable pageable);
    
    /**
     * Get all reviews by a user with pagination
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of reviews
     */
    Page<Review> getReviewsByUserId(Long userId, Pageable pageable);
    
    /**
     * Get a specific review by ID
     * @param reviewId the review ID
     * @return the review
     */
    Review getReviewById(Long reviewId);
    
    /**
     * Get user's review for a specific recipe
     * @param userId the user ID
     * @param recipeId the recipe ID
     * @return the review if exists, null otherwise
     */
    Review getUserReviewForRecipe(Long userId, Long recipeId);
    
    /**
     * Check if user has already reviewed a recipe
     * @param userId the user ID
     * @param recipeId the recipe ID
     * @return true if user has reviewed the recipe
     */
    boolean hasUserReviewedRecipe(Long userId, Long recipeId);
    
    /**
     * Get review summary statistics for a recipe
     * @param recipeId the recipe ID
     * @return summary with average rating, total reviews, and distribution
     */
    ReviewSummaryDTO getReviewSummary(Long recipeId);
    
    /**
     * Convert Review entity to ReviewDTO
     * @param review the review entity
     * @return the review DTO
     */
    ReviewDTO convertToDTO(Review review);
    
    /**
     * Update recipe's aggregated rating statistics
     * This is called automatically when reviews are created/updated/deleted
     * @param recipeId the recipe ID
     */
    void updateRecipeRatingStats(Long recipeId);
}
