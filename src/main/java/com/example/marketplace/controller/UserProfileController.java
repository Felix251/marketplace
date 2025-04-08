package com.example.marketplace.controller;

import com.example.marketplace.dto.user.*;
import com.example.marketplace.model.user.Address;
import com.example.marketplace.model.user.User;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> getCurrentUserProfile() {
        log.debug("Retrieving current user profile");
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(convertToDto(currentUser));
    }

    @PutMapping
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest updateRequest) {
        log.debug("Updating user profile: {}", updateRequest);
        User updatedUser = userService.updateProfile(updateRequest);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest passwordRequest) {
        log.debug("Processing password change request");
        userService.updatePassword(
                authService.getCurrentUser().getId(),
                passwordRequest.getCurrentPassword(),
                passwordRequest.getNewPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Password successfully updated"));
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddressRequest addressRequest) {
        log.debug("Adding new address: {}", addressRequest);
        Address newAddress = userService.addAddress(addressRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(newAddress));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest addressRequest) {
        log.debug("Updating address ID {}: {}", id, addressRequest);
        Address updatedAddress = userService.updateAddress(id, addressRequest);
        return ResponseEntity.ok(convertToDto(updatedAddress));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        log.debug("Deleting address ID: {}", id);
        userService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/avatar")
//    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
//        log.debug("Uploading avatar, filename: {}, size: {}", file.getOriginalFilename(), file.getSize());
//        String avatarUrl = userService.updateAvatar(file);
//        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
//    }

    private UserProfileDto convertToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getAddresses() != null) {
            dto.setAddresses(user.getAddresses().stream()
                    .map(this::convertToDto)
                    .toList());
        }

        return dto;
    }

    private AddressDto convertToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setIsDefault(address.getIsDefault());
//        dto.setLabel(address.getLabel());
        return dto;
    }
}