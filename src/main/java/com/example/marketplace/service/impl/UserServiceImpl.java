package com.example.marketplace.service.impl;

import com.example.marketplace.dto.user.AddressRequest;
import com.example.marketplace.dto.user.UserProfileUpdateRequest;
import com.example.marketplace.exception.AccessDeniedException;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.user.Address;
import com.example.marketplace.model.user.User;
import com.example.marketplace.model.user.UserRole;
import com.example.marketplace.repository.jpa.AddressRepository;
import com.example.marketplace.repository.jpa.UserRepository;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }

    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Fetching page of all users");
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role, pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword: {}", keyword);
        return userRepository.searchUsers(keyword, pageable);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        log.debug("Creating new user with email: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Email already in use: {}", user.getEmail());
            throw new IllegalArgumentException("Email already in use: " + user.getEmail());
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // By default, new users are enabled
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }

        // By default, new users are BUYERS unless specified
        if (user.getRole() == null) {
            user.setRole(UserRole.BUYER);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, User userDetails) {
        log.debug("Updating user with ID: {}", id);
        User user = getUserById(id);

        // Update user details except for sensitive fields like password and email
        // which should be updated through separate methods
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);
        return updatedUser;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        // Check if user exists
        getUserById(id);

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void changeUserRole(Long id, UserRole newRole) {
        log.debug("Changing role for user ID {} to {}", id, newRole);
        User user = getUserById(id);
        user.setRole(newRole);
        userRepository.save(user);
        log.info("User role changed: ID={}, newRole={}", id, newRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updatePassword(Long id, String currentPassword, String newPassword) {
        log.debug("Updating password for user ID: {}", id);
        User user = getUserById(id);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password update failed: incorrect current password for user ID: {}", id);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updateEmail(Long id, String newEmail, String password) {
        log.debug("Updating email for user ID: {} to: {}", id, newEmail);
        User user = getUserById(id);

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Email update failed: incorrect password for user ID: {}", id);
            throw new IllegalArgumentException("Password is incorrect");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            log.warn("Email update failed: email already in use: {}", newEmail);
            throw new IllegalArgumentException("Email already in use: " + newEmail);
        }

        // Update email
        user.setEmail(newEmail);
        userRepository.save(user);
        log.info("Email updated successfully for user ID: {} to: {}", id, newEmail);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void toggleUserStatus(Long id) {
        log.debug("Toggling status for user ID: {}", id);
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        log.info("User status toggled: ID={}, newStatus={}", id, user.getEnabled());
    }

    @Override
    public List<User> getNewUsers() {
        log.debug("Fetching new users");
        return userRepository.findNewUsers();
    }

    @Override
    public long countByRole(UserRole role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }

    // -------------------- Méthodes pour la gestion de profil -------------------

    @Override
    @Transactional
    public User updateProfile(UserProfileUpdateRequest updateRequest) {
        User currentUser = authService.getCurrentUser();
        log.debug("Updating profile for user ID {}: {}", currentUser.getId(), updateRequest);

        currentUser.setFirstName(updateRequest.getFirstName());
        currentUser.setLastName(updateRequest.getLastName());
        currentUser.setPhone(updateRequest.getPhone());

        User savedUser = userRepository.save(currentUser);
        log.info("Profile updated for user ID {}", currentUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public Address addAddress(AddressRequest addressRequest) {
        User currentUser = authService.getCurrentUser();
        log.debug("Adding address for user ID {}: {}", currentUser.getId(), addressRequest);

        Address address = new Address();
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setCountry(addressRequest.getCountry());
//        address.setLabel(addressRequest.getLabel());
        address.setUser(currentUser);

        // Si c'est l'adresse par défaut, mettre à jour les autres adresses
        if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
            currentUser.getAddresses().forEach(a -> a.setIsDefault(false));
            address.setIsDefault(true);
        } else if (currentUser.getAddresses().isEmpty()) {
            // Si c'est la première adresse, la définir par défaut
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        currentUser.getAddresses().add(address);
        userRepository.save(currentUser);

        log.info("Address added for user ID {}: address ID {}", currentUser.getId(), address.getId());
        return address;
    }

    @Override
    @Transactional
    public Address updateAddress(Long addressId, AddressRequest addressRequest) {
        User currentUser = authService.getCurrentUser();
        log.debug("Updating address ID {} for user ID {}: {}", addressId, currentUser.getId(), addressRequest);

        // Vérifier que l'adresse appartient à l'utilisateur
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address ID {} not found", addressId);
                    return new ResourceNotFoundException("Address not found");
                });

        if (!address.getUser().getId().equals(currentUser.getId())) {
            log.warn("Access denied: user ID {} attempted to access address ID {} owned by user ID {}",
                    currentUser.getId(), addressId, address.getUser().getId());
            throw new AccessDeniedException("You don't have permission to update this address");
        }

        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setCountry(addressRequest.getCountry());
//        address.setLabel(addressRequest.getLabel());

        // Gestion de l'adresse par défaut
        if (Boolean.TRUE.equals(addressRequest.getIsDefault()) && !address.getIsDefault()) {
            currentUser.getAddresses().forEach(a -> a.setIsDefault(false));
            address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address ID {} updated for user ID {}", addressId, currentUser.getId());
        return updatedAddress;
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        User currentUser = authService.getCurrentUser();
        log.debug("Deleting address ID {} for user ID {}", addressId, currentUser.getId());

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address ID {} not found", addressId);
                    return new ResourceNotFoundException("Address not found");
                });

        if (!address.getUser().getId().equals(currentUser.getId())) {
            log.warn("Access denied: user ID {} attempted to delete address ID {} owned by user ID {}",
                    currentUser.getId(), addressId, address.getUser().getId());
            throw new AccessDeniedException("You don't have permission to delete this address");
        }

        // Si c'est l'adresse par défaut et il y a d'autres adresses,
        // définir une autre adresse comme par défaut
        if (address.getIsDefault() && currentUser.getAddresses().size() > 1) {
            Optional<Address> newDefault = currentUser.getAddresses().stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst();

            newDefault.ifPresent(a -> a.setIsDefault(true));
        }

        currentUser.getAddresses().remove(address);
        addressRepository.delete(address);
        userRepository.save(currentUser);
        log.info("Address ID {} deleted for user ID {}", addressId, currentUser.getId());
    }
}