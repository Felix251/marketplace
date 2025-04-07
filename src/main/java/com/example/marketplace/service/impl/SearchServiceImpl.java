package com.example.marketplace.service.impl;

import com.example.marketplace.model.product.Product;
import com.example.marketplace.repository.jpa.ProductRepository;
import com.example.marketplace.repository.elasticsearch.ProductSearchRepository;
import com.example.marketplace.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    private static final String INDEX_NAME = "products";

    @Override
    public Page<Product> searchByNameOrDescription(String searchTerm, Pageable pageable) {
        return productSearchRepository.searchByNameOrDescription(searchTerm, pageable);
    }

    @Override
    public Page<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productSearchRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> searchByCategoryAndKeyword(String category, String keyword, Pageable pageable) {
        return productSearchRepository.searchByCategoryAndKeyword(category, keyword, pageable);
    }

    @Override
    public Page<Product> advancedSearch(String query, Pageable pageable) {
        return productSearchRepository.advancedSearch(query, pageable);
    }

    @Override
    public Page<Product> fuzzySearch(String searchTerm, Pageable pageable) {
        return productSearchRepository.fuzzySearch(searchTerm, pageable);
    }

    @Override
    public Map<String, List<String>> getSuggestions(String searchTerm) {
        // Simplified implementation of suggestions using basic search
        // Create a simple query with multi-match on name and description fields
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("name", "description")
                                .query(searchTerm)
                        )
                )
                .withPageable(Pageable.ofSize(10))
                .build();

        // Execute search
        SearchHits<Product> searchHits = elasticsearchOperations.search(
                searchQuery, Product.class);

        // Process results
        Map<String, List<String>> suggestionMap = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for (SearchHit<Product> hit : searchHits) {
            Product product = hit.getContent();
            if (product.getName() != null &&
                    product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                names.add(product.getName());
            }
            if (product.getDescription() != null &&
                    product.getDescription().toLowerCase().contains(searchTerm.toLowerCase())) {
                String snippet = product.getDescription();
                if (snippet.length() > 100) {
                    snippet = snippet.substring(0, 100) + "...";
                }
                descriptions.add(snippet);
            }
        }

        suggestionMap.put("names", names);
        suggestionMap.put("descriptions", descriptions);

        return suggestionMap;
    }

    @Override
    public void indexProduct(Product product) {
        productSearchRepository.save(product);
    }

    @Override
    public void deleteProductFromIndex(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    @Override
    @Async
    public void reindexAllProducts() {
        // Clear the index
        productSearchRepository.deleteAll();

        // Get all products from the database
        List<Product> products = productRepository.findAll();

        // Index all products
        productSearchRepository.saveAll(products);
    }

    @Override
    public long countIndexedProducts() {
        return productSearchRepository.count();
    }
}