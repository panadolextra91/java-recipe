package com.javarecipe.backend.interaction.service;

import com.javarecipe.backend.interaction.dto.ReviewDTO;
import com.javarecipe.backend.interaction.dto.ReviewSummaryDTO;
import com.javarecipe.backend.interaction.entity.Review;
import com.javarecipe.backend.interaction.repository.ReviewRepository;
import com.javarecipe.backend.notification.service.NotificationService;
import com.javarecipe.backend.recipe.entity.Recipe;
import com.javarecipe.backend.recipe.repository.RecipeRepository;
import com.javarecipe.backend.user.entity.User;
import com.javarecipe.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final NotificationService notificationService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             RecipeRepository recipeRepository,
                             NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Review createReview(Long recipeId, Integer rating, String comment, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        // Check if user already reviewed this recipe
        if (reviewRepository.existsByUserAndRecipe(user, recipe)) {
            throw new IllegalArgumentException("User has already reviewed this recipe");
        }
        
        Review review = new Review();
        review.setUser(user);
        review.setRecipe(recipe);
        review.setRating(rating);
        review.setComment(comment);
        
        Review savedReview = reviewRepository.save(review);

        // Update recipe's aggregated rating statistics
        updateRecipeRatingStats(recipeId);

        // Create notification for recipe owner
        notificationService.createRecipeReviewNotification(recipeId, userId);

        return savedReview;
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, Integer rating, String comment, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        
        // Check if user is authorized to update this review
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to update this review");
        }
        
        review.setRating(rating);
        review.setComment(comment);
        
        Review updatedReview = reviewRepository.save(review);
        
        // Update recipe's aggregated rating statistics
        updateRecipeRatingStats(review.getRecipe().getId());
        
        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        
        // Check if user is authorized to delete this review
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to delete this review");
        }
        
        Long recipeId = review.getRecipe().getId();
        reviewRepository.delete(review);
        
        // Update recipe's aggregated rating statistics
        updateRecipeRatingStats(recipeId);
    }

    @Override
    public Page<Review> getReviewsByRecipeId(Long recipeId, Pageable pageable) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return reviewRepository.findByRecipe(recipe, pageable);
    }

    @Override
    public Page<Review> getReviewsByUserId(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return reviewRepository.findByUser(user, pageable);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
    }

    @Override
    public Review getUserReviewForRecipe(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return reviewRepository.findByUserAndRecipe(user, recipe).orElse(null);
    }

    @Override
    public boolean hasUserReviewedRecipe(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        return reviewRepository.existsByUserAndRecipe(user, recipe);
    }

    @Override
    public ReviewSummaryDTO getReviewSummary(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        Double averageRating = reviewRepository.findAverageRatingByRecipe(recipe);
        long totalReviews = reviewRepository.countByRecipe(recipe);
        
        // Get rating distribution
        List<Object[]> distributionData = reviewRepository.findRatingDistributionByRecipeId(recipeId);
        Integer[] ratingDistribution = new Integer[5]; // For ratings 1-5
        
        // Initialize with zeros
        for (int i = 0; i < 5; i++) {
            ratingDistribution[i] = 0;
        }
        
        // Fill in actual counts
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            if (rating >= 1 && rating <= 5) {
                ratingDistribution[rating - 1] = count.intValue();
            }
        }
        
        ReviewSummaryDTO summary = new ReviewSummaryDTO();
        summary.setAverageRating(averageRating);
        summary.setTotalReviews((int) totalReviews);
        summary.setRatingDistribution(ratingDistribution);
        return summary;
    }

    @Override
    public ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserAvatarUrl(review.getUser().getAvatarUrl());
        dto.setRecipeId(review.getRecipe().getId());
        dto.setRecipeTitle(review.getRecipe().getTitle());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public void updateRecipeRatingStats(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + recipeId));
        
        Double averageRating = reviewRepository.findAverageRatingByRecipe(recipe);
        long reviewCount = reviewRepository.countByRecipe(recipe);
        
        recipe.setAverageRating(averageRating);
        recipe.setReviewCount((int) reviewCount);
        
        recipeRepository.save(recipe);
    }
}
