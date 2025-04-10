package com.example.marketplace.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private List<String> images;
    private Boolean featured;
    private Boolean active;
    private Long storeId;
    private String storeName;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private LocalDateTime createdAt;
}