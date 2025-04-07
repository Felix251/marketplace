package com.example.marketplace.repository.jpa;

import com.example.marketplace.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE p.store.id = :storeId")
    List<OrderItem> findByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long sumQuantityByProduct(@Param("productId") Long productId);

    @Query(value = "SELECT p.id, p.name, SUM(oi.quantity) as total_quantity " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY total_quantity DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findBestSellingProducts(@Param("limit") int limit);

    @Query(value = "SELECT c.id, c.name, SUM(oi.quantity) as quantity " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN product_categories pc ON p.id = pc.product_id " +
            "JOIN categories c ON pc.category_id = c.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY c.id, c.name " +
            "ORDER BY quantity DESC",
            nativeQuery = true)
    List<Object[]> findSalesByCategory();
}