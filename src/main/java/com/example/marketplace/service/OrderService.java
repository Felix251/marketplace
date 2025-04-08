package com.example.marketplace.service;

import com.example.marketplace.model.order.Order;
import com.example.marketplace.model.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {

    Order getOrderById(Long id);

    Order getOrderByOrderNumber(String orderNumber);

    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);

    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    Page<Order> getOrdersByTotalGreaterThan(BigDecimal amount, Pageable pageable);

    Order createOrderFromCart(Long userId, Long shippingAddressId, Long billingAddressId);

    Order updateOrderStatus(Long id, OrderStatus status);

    Order updateTrackingNumber(Long id, String trackingNumber);

    void cancelOrder(Long id);

    List<Map<String, Object>> getOrderCountByStoreInDateRange(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getMonthlySalesReport();

    long countPendingOrders();

    long countNewOrders();
}