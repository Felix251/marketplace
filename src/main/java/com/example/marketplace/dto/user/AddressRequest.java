package com.example.marketplace.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Street address cannot be blank")
    private String street;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "State/Region cannot be blank")
    private String state;

    @NotBlank(message = "Zip/Postal code cannot be blank")
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    private Boolean isDefault;
//    private String label; // ex: "Home", "Work"
}