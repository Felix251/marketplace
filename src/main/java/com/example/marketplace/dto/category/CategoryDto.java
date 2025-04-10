package com.example.marketplace.dto.category;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Boolean active;
    private Long parentId;
    private String parentName;
}