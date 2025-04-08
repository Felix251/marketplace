package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.cart.Wishlist;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.user.User;
import com.example.marketplace.repository.jpa.WishlistRepository;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.UserService;
import com.example.marketplace.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    public Wishlist getWishlistById(Long id) {
        return wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found with id: " + id));
    }

    @Override
    public List<Wishlist> getWishlistsByUserId(Long userId) {
        // Verify user exists
        userService.getUserById(userId);

        return wishlistRepository.findByUserId(userId);
    }

    @Override
    public Wishlist getWishlistByUserIdAndName(Long userId, String name) {
        return wishlistRepository.findByUserIdAndName(userId, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist not found with name: " + name + " for user id: " + userId));
    }

    @Override
    public List<Wishlist> getWishlistsByUserIdAndProductId(Long userId, Long productId) {
        return wishlistRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public Wishlist createWishlist(Long userId, String name) {
        User user = userService.getUserById(userId);

        // Check if wishlist with same name already exists
        if (wishlistRepository.findByUserIdAndName(userId, name).isPresent()) {
            throw new IllegalArgumentException("Wishlist with name '" + name + "' already exists");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setName(name);

        return wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public Wishlist updateWishlist(Long id, String name) {
        Wishlist wishlist = getWishlistById(id);

        // Check if new name conflicts with existing wishlists
        if (!wishlist.getName().equals(name) &&
                wishlistRepository.findByUserIdAndName(wishlist.getUser().getId(), name).isPresent()) {
            throw new IllegalArgumentException("Wishlist with name '" + name + "' already exists");
        }

        wishlist.setName(name);
        return wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void deleteWishlist(Long id) {
        // Verify wishlist exists
        getWishlistById(id);

        wishlistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Wishlist addProductToWishlist(Long wishlistId, Long productId) {
        Wishlist wishlist = getWishlistById(wishlistId);
        Product product = productService.getProductById(productId);

        // Check if product is already in wishlist
        if (wishlist.getProducts().stream().anyMatch(p -> p.getId().equals(productId))) {
            // Product already in wishlist, no need to add again
            return wishlist;
        }

        wishlist.getProducts().add(product);
        return wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public Wishlist removeProductFromWishlist(Long wishlistId, Long productId) {
        Wishlist wishlist = getWishlistById(wishlistId);

        // Remove product
        wishlist.getProducts().removeIf(product -> product.getId().equals(productId));

        return wishlistRepository.save(wishlist);
    }

    @Override
    public boolean isProductInWishlist(Long wishlistId, Long productId) {
        Wishlist wishlist = getWishlistById(wishlistId);

        return wishlist.getProducts().stream()
                .anyMatch(product -> product.getId().equals(productId));
    }

    @Override
    public long countWishlistsByProduct(Long productId) {
        // Verify product exists
        productService.getProductById(productId);

        return wishlistRepository.countWishlistsByProduct(productId);
    }

    @Override
    public List<Map<String, Object>> getMostWishedProducts(int limit) {
        List<Object[]> results = wishlistRepository.findMostWishedProducts(limit);
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", result[0]);
            map.put("productName", result[1]);
            map.put("wishCount", result[2]);
            mappedResults.add(map);
        }

        return mappedResults;
    }

    @Override
    public long countNewWishlists() {
        return wishlistRepository.countNewWishlists();
    }
}