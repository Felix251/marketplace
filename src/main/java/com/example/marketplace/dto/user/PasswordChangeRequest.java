package com.example.marketplace.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordChangeRequest {
    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password cannot be blank")
    private String confirmPassword;
}