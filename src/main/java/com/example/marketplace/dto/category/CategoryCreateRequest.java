package com.example.marketplace.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String description;
    private String image;
    private Long parentId;
}