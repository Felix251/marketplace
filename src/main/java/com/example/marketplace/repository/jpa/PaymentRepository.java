package com.example.marketplace.repository.jpa;

import com.example.marketplace.model.payment.Payment;
import com.example.marketplace.model.payment.PaymentMethod;
import com.example.marketplace.model.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByMethod(PaymentMethod method);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.amount > :amount")
    List<Payment> findByAmountGreaterThan(@Param("amount") BigDecimal amount);

    @Query(value = "SELECT payment_method, COUNT(*) as count, SUM(amount) as total " +
            "FROM payments " +
            "WHERE payment_date BETWEEN :startDate AND :endDate AND status = 'COMPLETED' " +
            "GROUP BY payment_method",
            nativeQuery = true)
    List<Object[]> getPaymentMethodStats(@Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    @Query(value = "SELECT COUNT(*) FROM payments WHERE status = 'FAILED' AND created_at > current_date - interval '24 hour'",
            nativeQuery = true)
    long countRecentFailedPayments();

    @Query(value = "SELECT SUM(amount) FROM payments WHERE status = 'COMPLETED' AND payment_date > current_date - interval '30 day'",
            nativeQuery = true)
    BigDecimal sumRecentSuccessfulPayments();
}