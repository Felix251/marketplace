package com.example.marketplace.model.payment;

public enum PaymentStatus {
    PENDING,    // Paiement en attente
    COMPLETED,  // Paiement effectué avec succès
    FAILED,     // Échec du paiement
    REFUNDED,   // Paiement remboursé
    CANCELLED   // Paiement annulé
}