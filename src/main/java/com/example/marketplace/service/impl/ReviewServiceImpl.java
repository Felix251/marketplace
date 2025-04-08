package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.order.Order;
import com.example.marketplace.model.order.OrderStatus;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.product.Review;
import com.example.marketplace.model.user.User;
import com.example.marketplace.repository.jpa.ReviewRepository;
import com.example.marketplace.service.OrderService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.ReviewService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    @Override
    public Page<Review> getReviewsByProductId(Long productId, Pageable pageable) {
        // Verify product exists
        productService.getProductById(productId);

        return reviewRepository.findByProductId(productId, pageable);
    }

    @Override
    public Page<Review> getReviewsByUserId(Long userId, Pageable pageable) {
        // Verify user exists
        userService.getUserById(userId);

        return reviewRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Review> getReviewsByProductIdAndRating(Long productId, Integer rating, Pageable pageable) {
        // Verify product exists
        productService.getProductById(productId);

        return reviewRepository.findByProductIdAndRating(productId, rating, pageable);
    }

    @Override
    public Page<Review> getReviewsByStoreId(Long storeId, Pageable pageable) {
        return reviewRepository.findByStoreId(storeId, pageable);
    }

    @Override
    @Transactional
    public Review createReview(Long userId, Long productId, Review review) {
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(productId);

        // Check if user has purchased the product
        if (!canUserReviewProduct(userId, productId)) {
            throw new IllegalArgumentException("User must purchase the product before reviewing it");
        }

        // Check if user has already reviewed this product
        Page<Review> existingReviews = reviewRepository.findByUserId(userId, Pageable.unpaged());
        boolean hasReviewed = existingReviews.stream()
                .anyMatch(r -> r.getProduct().getId().equals(productId));

        if (hasReviewed) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }

        // Set relationships
        review.setUser(user);
        review.setProduct(product);

        // Validate rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = getReviewById(id);

        // Update review fields
        if (reviewDetails.getRating() != null) {
            if (reviewDetails.getRating() < 1 || reviewDetails.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            review.setRating(reviewDetails.getRating());
        }

        if (reviewDetails.getComment() != null) {
            review.setComment(reviewDetails.getComment());
        }

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        // Verify review exists
        Review review = getReviewById(id);

        reviewRepository.deleteById(id);
    }

    @Override
    public Double calculateAverageRatingForProduct(Long productId) {
        // Verify product exists
        productService.getProductById(productId);

        return reviewRepository.calculateAverageRatingForProduct(productId);
    }

    @Override
    public Map<Integer, Long> getRatingDistributionForProduct(Long productId) {
        // Verify product exists
        productService.getProductById(productId);

        List<Long> counts = reviewRepository.countRatingsByProduct(productId);

        // Initialize all ratings from 1 to 5 with count 0
        Map<Integer, Long> distribution = IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toMap(i -> i, i -> 0L));

        // Fill in actual counts (if any)
        if (counts != null && !counts.isEmpty()) {
            for (int i = 0; i < counts.size(); i++) {
                distribution.put(5 - i, counts.get(i));
            }
        }

        return distribution;
    }

    @Override
    public List<Map<String, Object>> getTopRatedProducts(int minReviews, int limit) {
        List<Object[]> results = reviewRepository.findTopRatedProducts(minReviews, limit);
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", result[0]);
            map.put("productName", result[1]);
            map.put("averageRating", result[2]);
            map.put("reviewCount", result[3]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public long countNewReviews() {
        return reviewRepository.countNewReviews();
    }

    @Override
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Check if user has purchased and received the product
        Page<Order> userOrders = orderService.getOrdersByUserId(userId, Pageable.unpaged());

        return userOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}