package com.example.marketplace.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSearchCriteria {
    private String keyword;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long storeId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean featured;
    private Boolean inStock;
    private Boolean active;
    private List<String> sortFields;
    private List<String> sortDirections;

    // Valeurs par défaut pour la pagination
    private Integer page = 0;
    private Integer size = 10;
}