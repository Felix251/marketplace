package com.example.marketplace.repository;

import com.example.marketplace.model.product.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating = :rating")
    Page<Review> findByProductIdAndRating(@Param("productId") Long productId,
                                          @Param("rating") Integer rating,
                                          Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Long> countRatingsByProduct(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.product.store.id = :storeId")
    Page<Review> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    @Query(value = "SELECT p.id, p.name, AVG(r.rating) as avg_rating, COUNT(r.id) as review_count " +
            "FROM reviews r " +
            "JOIN products p ON r.product_id = p.id " +
            "GROUP BY p.id, p.name " +
            "HAVING COUNT(r.id) >= :minReviews " +
            "ORDER BY avg_rating DESC, review_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopRatedProducts(@Param("minReviews") int minReviews, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM reviews WHERE created_at > current_date - interval '7 day'",
            nativeQuery = true)
    long countNewReviews();
}