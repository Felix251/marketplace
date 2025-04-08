package com.example.marketplace.service;

import com.example.marketplace.model.product.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    Review getReviewById(Long id);

    Page<Review> getReviewsByProductId(Long productId, Pageable pageable);

    Page<Review> getReviewsByUserId(Long userId, Pageable pageable);

    Page<Review> getReviewsByProductIdAndRating(Long productId, Integer rating, Pageable pageable);

    Page<Review> getReviewsByStoreId(Long storeId, Pageable pageable);

    Review createReview(Long userId, Long productId, Review review);

    Review updateReview(Long id, Review reviewDetails);

    void deleteReview(Long id);

    Double calculateAverageRatingForProduct(Long productId);

    Map<Integer, Long> getRatingDistributionForProduct(Long productId);

    List<Map<String, Object>> getTopRatedProducts(int minReviews, int limit);

    long countNewReviews();

    boolean canUserReviewProduct(Long userId, Long productId);
}