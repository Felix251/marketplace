package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.cart.Cart;
import com.example.marketplace.model.cart.CartItem;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.user.User;
import com.example.marketplace.repository.jpa.CartItemRepository;
import com.example.marketplace.repository.jpa.CartRepository;
import com.example.marketplace.service.CartService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));
    }

    @Override
    public Cart getActiveCartByUserId(Long userId) {
        // Check if user has an active cart
        Optional<Cart> existingCart = cartRepository.findActiveCartByUserId(userId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create a new cart if none exists
        User user = userService.getUserById(userId);
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setActive(true);

        return cartRepository.save(newCart);
    }

    @Override
    @Transactional
    public CartItem addItemToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Check if product exists and is available
        Product product = productService.getProductById(productId);
        if (!product.getActive()) {
            throw new IllegalArgumentException("Product is not active");
        }
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        // Get or create cart
        Cart cart = getActiveCartByUserId(userId);

        // Check if product is already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            // Update quantity if already in cart
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Check if new quantity exceeds available stock
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);

            return cartItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Check if item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the user's cart");
        }

        // Check if quantity is available
        Product product = cartItem.getProduct();
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        // Update quantity
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Check if item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the user's cart");
        }

        // Remove item
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Delete all items
        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    @Override
    public BigDecimal getCartTotal(Long userId) {
        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Calculate total
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCartItemCount(Long userId) {
        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Count items
        Integer count = cartItemRepository.sumQuantityByCartId(cart.getId());
        return count != null ? count : 0;
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        // Get active cart
        Cart cart = getActiveCartByUserId(userId);

        // Get items
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    public List<Cart> getAbandonedCarts() {
        return cartRepository.findAbandonedCarts();
    }

    @Override
    public List<Map<String, Object>> getMostAddedToCartProducts(int limit) {
        List<Object[]> results = cartRepository.findMostAddedToCartProducts(limit);
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", result[0]);
            map.put("productName", result[1]);
            map.put("frequency", result[2]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public long countRecentActiveCarts() {
        return cartRepository.countRecentActiveCarts();
    }
}