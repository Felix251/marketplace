package com.example.marketplace.repository;

import com.example.marketplace.model.order.Order;
import com.example.marketplace.model.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") OrderStatus status,
                                      Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT o FROM Order o WHERE o.total > :amount")
    Page<Order> findByTotalGreaterThan(@Param("amount") BigDecimal amount, Pageable pageable);

    @Query(value = "SELECT p.store_id, COUNT(o.id) as order_count " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY p.store_id " +
            "ORDER BY order_count DESC",
            nativeQuery = true)
    List<Object[]> findOrderCountByStoreInDateRange(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    @Query(value = "SELECT EXTRACT(MONTH FROM o.order_date) as month, " +
            "EXTRACT(YEAR FROM o.order_date) as year, " +
            "SUM(o.total) as total_sales " +
            "FROM orders o " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY month, year " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> getMonthlySalesReport();

    @Query(value = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'", nativeQuery = true)
    long countPendingOrders();

    @Query(value = "SELECT COUNT(*) FROM orders WHERE created_at > current_date - interval '24 hour'", nativeQuery = true)
    long countNewOrders();
}