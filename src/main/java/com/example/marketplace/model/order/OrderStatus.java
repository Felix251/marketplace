package com.example.marketplace.model.order;

public enum OrderStatus {
    PENDING,        // Commande créée mais non confirmée
    PAID,           // Commande payée mais non traitée
    PROCESSING,     // Commande en cours de traitement
    SHIPPED,        // Commande expédiée
    DELIVERED,      // Commande livrée
    CANCELLED,      // Commande annulée
    REFUNDED        // Commande remboursée
}