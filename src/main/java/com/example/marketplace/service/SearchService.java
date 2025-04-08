package com.example.marketplace.service;

import com.example.marketplace.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SearchService {

    Page<Product> searchByNameOrDescription(String searchTerm, Pageable pageable);

    Page<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> searchByCategoryAndKeyword(String category, String keyword, Pageable pageable);

    Page<Product> advancedSearch(String query, Pageable pageable);

    Page<Product> fuzzySearch(String searchTerm, Pageable pageable);

    Map<String, List<String>> getSuggestions(String searchTerm);

    void indexProduct(Product product);

    void deleteProductFromIndex(Long productId);

    void reindexAllProducts();

    long countIndexedProducts();
}