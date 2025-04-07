package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.order.Order;
import com.example.marketplace.model.order.OrderStatus;
import com.example.marketplace.model.payment.Payment;
import com.example.marketplace.model.payment.PaymentMethod;
import com.example.marketplace.model.payment.PaymentStatus;
import com.example.marketplace.repository.jpa.PaymentRepository;
import com.example.marketplace.service.OrderService;
import com.example.marketplace.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${paypal.client.id}")
    private String paypalClientId;

    @Value("${paypal.client.secret}")
    private String paypalClientSecret;

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction id: " + transactionId));
    }

    @Override
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public List<Payment> getPaymentsByMethod(PaymentMethod method) {
        return paymentRepository.findByMethod(method);
    }

    @Override
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Payment> getPaymentsByAmountGreaterThan(BigDecimal amount) {
        return paymentRepository.findByAmountGreaterThan(amount);
    }

    @Override
    @Transactional
    public Payment createPayment(Long orderId, PaymentMethod method, Map<String, Object> paymentDetails) {
        Order order = orderService.getOrderById(orderId);

        // Create new payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotal());
        payment.setPaymentDate(LocalDateTime.now());

        // Generate transaction ID
        payment.setTransactionId(generateTransactionId());

        // Store payment details as JSON
        try {
            payment.setProviderData(objectMapper.writeValueAsString(paymentDetails));
        } catch (Exception e) {
            throw new RuntimeException("Error storing payment details", e);
        }

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = getPaymentById(id);

        // Update status
        payment.setStatus(status);

        // Update order status if payment is completed or failed
        if (status == PaymentStatus.COMPLETED) {
            orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.PAID);
        } else if (status == PaymentStatus.FAILED) {
            // Payment failed but order remains in PENDING status
            // This may require manual intervention or automatic retry
        } else if (status == PaymentStatus.REFUNDED) {
            orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.REFUNDED);
        }

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processStripePayment(Long orderId, String stripeToken) {
        Order order = orderService.getOrderById(orderId);

        // In a real implementation, you would:
        // 1. Use the Stripe API to create a charge using the token
        // 2. Handle any potential exceptions from Stripe

        // For this example, we'll simulate a successful payment
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("stripeToken", stripeToken);
        paymentDetails.put("orderId", orderId);

        Payment payment = createPayment(orderId, PaymentMethod.STRIPE, paymentDetails);

        // Simulate successful payment processing
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        // Update order status
        orderService.updateOrderStatus(orderId, OrderStatus.PAID);

        return payment;
    }

    @Override
    @Transactional
    public Payment processPayPalPayment(Long orderId, String paypalPaymentId) {
        Order order = orderService.getOrderById(orderId);

        // In a real implementation, you would:
        // 1. Use the PayPal API to execute the payment
        // 2. Handle any potential exceptions from PayPal

        // For this example, we'll simulate a successful payment
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("paypalPaymentId", paypalPaymentId);
        paymentDetails.put("orderId", orderId);

        Payment payment = createPayment(orderId, PaymentMethod.PAYPAL, paymentDetails);

        // Simulate successful payment processing
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        // Update order status
        orderService.updateOrderStatus(orderId, OrderStatus.PAID);

        return payment;
    }

    @Override
    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);

        // Can only refund completed payments
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot refund payment with status: " + payment.getStatus());
        }

        // In a real implementation, you would:
        // 1. Use the appropriate payment API to process the refund
        // 2. Handle any potential exceptions

        // Update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        // Update order status
        orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.REFUNDED);

        return payment;
    }

    @Override
    public List<Map<String, Object>> getPaymentMethodStats(LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        List<Object[]> results = paymentRepository.getPaymentMethodStats(startDateStr, endDateStr);
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("paymentMethod", result[0]);
            map.put("count", result[1]);
            map.put("total", result[2]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public long countRecentFailedPayments() {
        return paymentRepository.countRecentFailedPayments();
    }

    @Override
    public BigDecimal sumRecentSuccessfulPayments() {
        BigDecimal sum = paymentRepository.sumRecentSuccessfulPayments();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}