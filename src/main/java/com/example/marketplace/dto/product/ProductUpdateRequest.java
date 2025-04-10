package com.example.marketplace.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String description;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    private List<String> images;
    private Boolean featured;
    private Boolean active;
    private List<Long> categoryIds;
}