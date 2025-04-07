package com.example.marketplace.service;

import com.example.marketplace.model.cart.Wishlist;

import java.util.List;
import java.util.Map;

public interface WishlistService {

    Wishlist getWishlistById(Long id);

    List<Wishlist> getWishlistsByUserId(Long userId);

    Wishlist getWishlistByUserIdAndName(Long userId, String name);

    List<Wishlist> getWishlistsByUserIdAndProductId(Long userId, Long productId);

    Wishlist createWishlist(Long userId, String name);

    Wishlist updateWishlist(Long id, String name);

    void deleteWishlist(Long id);

    Wishlist addProductToWishlist(Long wishlistId, Long productId);

    Wishlist removeProductFromWishlist(Long wishlistId, Long productId);

    boolean isProductInWishlist(Long wishlistId, Long productId);

    long countWishlistsByProduct(Long productId);

    List<Map<String, Object>> getMostWishedProducts(int limit);

    long countNewWishlists();
}