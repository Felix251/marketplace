package com.example.marketplace.repository;

import com.example.marketplace.model.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    void deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    long countItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);

    @Query(value = "SELECT p.id, p.name, COUNT(ci.id) as frequency " +
            "FROM cart_items ci " +
            "JOIN products p ON ci.product_id = p.id " +
            "WHERE ci.created_at > current_date - interval '7 day' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY frequency DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTrendingProductsInCart(@Param("limit") int limit);
}