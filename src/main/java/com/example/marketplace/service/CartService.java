package com.example.marketplace.service;

import com.example.marketplace.model.cart.Cart;
import com.example.marketplace.model.cart.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {

    Cart getCartByUserId(Long userId);

    Cart getActiveCartByUserId(Long userId);

    CartItem addItemToCart(Long userId, Long productId, Integer quantity);

    CartItem updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);

    void removeItemFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);

    BigDecimal getCartTotal(Long userId);

    int getCartItemCount(Long userId);

    List<CartItem> getCartItems(Long userId);

    List<Cart> getAbandonedCarts();

    List<Map<String, Object>> getMostAddedToCartProducts(int limit);

    long countRecentActiveCarts();
}