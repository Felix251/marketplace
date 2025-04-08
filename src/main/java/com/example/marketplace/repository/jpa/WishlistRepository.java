package com.example.marketplace.repository.jpa;

import com.example.marketplace.model.cart.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndName(Long userId, String name);

    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND :productId MEMBER OF w.products")
    List<Wishlist> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("SELECT COUNT(w) FROM Wishlist w JOIN w.products p WHERE p.id = :productId")
    long countWishlistsByProduct(@Param("productId") Long productId);

    @Query(value = "SELECT p.id, p.name, COUNT(wp.product_id) as wish_count " +
            "FROM wishlist_products wp " +
            "JOIN products p ON wp.product_id = p.id " +
            "GROUP BY p.id, p.name " +
            "ORDER BY wish_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findMostWishedProducts(@Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM wishlists WHERE created_at > current_date - interval '30 day'",
            nativeQuery = true)
    long countNewWishlists();
}