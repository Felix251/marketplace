package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.cart.Cart;
import com.example.marketplace.model.cart.CartItem;
import com.example.marketplace.model.order.Order;
import com.example.marketplace.model.order.OrderItem;
import com.example.marketplace.model.order.OrderStatus;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.user.Address;
import com.example.marketplace.model.user.User;
import com.example.marketplace.repository.jpa.OrderRepository;
import com.example.marketplace.service.AddressService;
import com.example.marketplace.service.CartService;
import com.example.marketplace.service.OrderService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CartService cartService;
    private final ProductService productService;

    // Tax rate (e.g., 8.25%)
    private static final BigDecimal TAX_RATE = new BigDecimal("0.0825");

    // Flat shipping fee (could be replaced with more sophisticated calculation)
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("5.99");

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
    }

    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        // Verify user exists
        userService.getUserById(userId);

        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable) {
        // Verify user exists
        userService.getUserById(userId);

        return orderRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public Page<Order> getOrdersByTotalGreaterThan(BigDecimal amount, Pageable pageable) {
        return orderRepository.findByTotalGreaterThan(amount, pageable);
    }

    @Override
    @Transactional
    public Order createOrderFromCart(Long userId, Long shippingAddressId, Long billingAddressId) {
        User user = userService.getUserById(userId);
        Address shippingAddress = addressService.getUserAddressById(userId, shippingAddressId);

        // Billing address can be the same as shipping address
        Address billingAddress = (billingAddressId != null && !billingAddressId.equals(shippingAddressId))
                ? addressService.getUserAddressById(userId, billingAddressId)
                : shippingAddress;

        // Get active cart with items
        Cart cart = cartService.getActiveCartByUserId(userId);
        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());

        // Set initial values
        order.setSubtotal(BigDecimal.ZERO);
        order.setTax(BigDecimal.ZERO);
        order.setShipping(SHIPPING_FEE);
        order.setTotal(BigDecimal.ZERO);

        // Save order first to get ID
        order = orderRepository.save(order);

        // Add items from cart to order
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Check product availability
            if (!product.getActive() || product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Product " + product.getName() + " is not available in the requested quantity");
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // Use current price

            order.getItems().add(orderItem);

            // Update subtotal
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            // Update product stock
            productService.updateProductStock(product.getId(), -cartItem.getQuantity());
        }

        // Calculate order totals
        order.setSubtotal(subtotal);
        order.setTax(subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP));
        order.setTotal(subtotal.add(order.getTax()).add(order.getShipping()));

        // Save updated order
        order = orderRepository.save(order);

        // Clear the cart after successful order creation
        cartService.clearCart(userId);

        return order;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        // Update status
        order.setStatus(status);

        // If order is delivered, set delivery date
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDate.now());
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateTrackingNumber(Long id, String trackingNumber) {
        Order order = getOrderById(id);
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);

        // Can only cancel if still in PENDING or PAID status
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Cannot cancel order in " + order.getStatus() + " status");
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);

        // Restore product stock
        for (OrderItem item : order.getItems()) {
            productService.updateProductStock(item.getProduct().getId(), item.getQuantity());
        }

        orderRepository.save(order);
    }

    @Override
    public List<Map<String, Object>> getOrderCountByStoreInDateRange(LocalDate startDate, LocalDate endDate) {
        String startDateStr = startDate.toString();
        String endDateStr = endDate.toString();

        List<Object[]> results = orderRepository.findOrderCountByStoreInDateRange(startDateStr, endDateStr);
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("storeId", result[0]);
            map.put("orderCount", result[1]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public List<Map<String, Object>> getMonthlySalesReport() {
        List<Object[]> results = orderRepository.getMonthlySalesReport();
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", result[0]);
            map.put("year", result[1]);
            map.put("totalSales", result[2]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public long countPendingOrders() {
        return orderRepository.countPendingOrders();
    }

    @Override
    public long countNewOrders() {
        return orderRepository.countNewOrders();
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PAID:
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case DELIVERED:
                throw new IllegalArgumentException("Cannot change status of a delivered order");
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status of a cancelled order");
            case REFUNDED:
                throw new IllegalArgumentException("Cannot change status of a refunded order");
            default:
                throw new IllegalArgumentException("Unknown order status: " + currentStatus);
        }
    }
}