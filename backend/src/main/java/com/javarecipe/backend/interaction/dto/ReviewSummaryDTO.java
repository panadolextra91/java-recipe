package com.javarecipe.backend.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {
    private Double averageRating;
    private Integer totalReviews;
    private Integer[] ratingDistribution; // Array of 5 elements for ratings 1-5

    // Explicit setters for IDE compatibility
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public void setRatingDistribution(Integer[] ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }
}
