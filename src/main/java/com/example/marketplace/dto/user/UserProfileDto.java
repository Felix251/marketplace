package com.example.marketplace.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private List<AddressDto> addresses;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}