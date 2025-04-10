package com.example.marketplace.dto.store;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreCreateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String description;
    private String logo;
    private String banner;
}