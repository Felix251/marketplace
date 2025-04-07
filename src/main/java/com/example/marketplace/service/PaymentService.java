package com.example.marketplace.service;

import com.example.marketplace.model.payment.Payment;
import com.example.marketplace.model.payment.PaymentMethod;
import com.example.marketplace.model.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    Payment getPaymentById(Long id);

    Payment getPaymentByTransactionId(String transactionId);

    List<Payment> getPaymentsByOrderId(Long orderId);

    List<Payment> getPaymentsByStatus(PaymentStatus status);

    List<Payment> getPaymentsByMethod(PaymentMethod method);

    List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> getPaymentsByAmountGreaterThan(BigDecimal amount);

    Payment createPayment(Long orderId, PaymentMethod method, Map<String, Object> paymentDetails);

    Payment updatePaymentStatus(Long id, PaymentStatus status);

    Payment processStripePayment(Long orderId, String stripeToken);

    Payment processPayPalPayment(Long orderId, String paypalPaymentId);

    Payment refundPayment(Long id);

    List<Map<String, Object>> getPaymentMethodStats(LocalDateTime startDate, LocalDateTime endDate);

    long countRecentFailedPayments();

    BigDecimal sumRecentSuccessfulPayments();
}