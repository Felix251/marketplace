package com.example.marketplace.repository;

import com.example.marketplace.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.math.BigDecimal;

public interface ProductSearchRepository extends ElasticsearchRepository<Product, Long> {

    // Recherche par nom ou description avec score de pertinence
    @Query("{\"bool\": {\"should\": [" +
            "{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 2.0}}}," +
            "{\"match\": {\"description\": \"?0\"}}" +
            "]}}")
    Page<Product> searchByNameOrDescription(String searchTerm, Pageable pageable);

    // Recherche par plage de prix
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Recherche par catégorie et mot-clé
    @Query("{\"bool\": {\"must\": [" +
            "{\"match\": {\"categories.name\": \"?0\"}}," +
            "{\"multi_match\": {\"query\": \"?1\", \"fields\": [\"name\", \"description\"]}}" +
            "]}}")
    Page<Product> searchByCategoryAndKeyword(String category, String keyword, Pageable pageable);

    // Recherche avec filtres sur plusieurs champs
    @Query("{\"bool\": {\"must\": [{\"match\": {\"active\": true}}]," +
            "\"should\": [" +
            "{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 3.0}}}," +
            "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1.0}}}," +
            "{\"match\": {\"store.name\": {\"query\": \"?0\", \"boost\": 0.5}}}" +
            "]," +
            "\"minimum_should_match\": 1}}")
    Page<Product> advancedSearch(String query, Pageable pageable);

    // Recherche avec suggestion (Did you mean...)
    @Query("{\"bool\": {\"should\": [" +
            "{\"match\": {\"name\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"description\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}" +
            "]}}")
    Page<Product> fuzzySearch(String searchTerm, Pageable pageable);
}