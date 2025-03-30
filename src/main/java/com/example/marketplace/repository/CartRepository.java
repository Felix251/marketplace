package com.example.marketplace.repository;

import com.example.marketplace.model.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c WHERE c.active = true AND c.user.id = :userId")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c WHERE c.active = true AND SIZE(c.items) > 0")
    List<Cart> findNonEmptyActiveCarts();

    @Query(value = "SELECT c.* FROM carts c " +
            "JOIN cart_items ci ON c.id = ci.cart_id " +
            "WHERE c.updated_at < current_timestamp - interval '7 day' " +
            "AND c.active = true " +
            "GROUP BY c.id",
            nativeQuery = true)
    List<Cart> findAbandonedCarts();

    @Query(value = "SELECT p.id, p.name, COUNT(ci.product_id) as frequency " +
            "FROM cart_items ci " +
            "JOIN products p ON ci.product_id = p.id " +
            "GROUP BY p.id, p.name " +
            "ORDER BY frequency DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findMostAddedToCartProducts(@Param("limit") int limit);

    @Query(value = "SELECT COUNT(c.id) " +
            "FROM carts c " +
            "WHERE c.active = true " +
            "AND c.updated_at > current_timestamp - interval '24 hour'",
            nativeQuery = true)
    long countRecentActiveCarts();
}