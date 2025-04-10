package com.example.marketplace.dto.store;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreDto {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String banner;
    private Boolean active;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
}