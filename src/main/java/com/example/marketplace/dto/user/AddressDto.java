package com.example.marketplace.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}