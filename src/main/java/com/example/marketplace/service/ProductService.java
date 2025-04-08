package com.example.marketplace.service;

import com.example.marketplace.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Product getProductById(Long id);

    Page<Product> getAllProducts(Pageable pageable);

    Page<Product> getProductsByStore(Long storeId, Pageable pageable);

    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<Product> getProductsByCategoryName(String categoryName, Pageable pageable);

    Page<Product> searchProducts(String keyword, Pageable pageable);

    Page<Product> getProductsByName(String name, Pageable pageable);

    Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> getFeaturedProducts(Pageable pageable);

    Page<Product> getAvailableProducts(Pageable pageable);

    Product createProduct(Long storeId, Product product, List<Long> categoryIds);

    Product updateProduct(Long id, Product productDetails, List<Long> categoryIds);

    void deleteProduct(Long id);

    void toggleProductStatus(Long id);

    void updateProductStock(Long id, Integer quantity);

    List<Product> getTopSellingProducts(int limit);

    List<Product> getNewProducts();

    boolean isProductAvailable(Long id, Integer quantity);
}