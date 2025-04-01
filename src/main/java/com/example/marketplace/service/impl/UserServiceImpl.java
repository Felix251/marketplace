package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.user.User;
import com.example.marketplace.model.user.UserRole;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
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

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, User userDetails) {
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

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // Check if user exists
        getUserById(id);

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void changeUserRole(Long id, UserRole newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updatePassword(Long id, String currentPassword, String newPassword) {
        User user = getUserById(id);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updateEmail(Long id, String newEmail, String password) {
        User user = getUserById(id);

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new IllegalArgumentException("Email already in use: " + newEmail);
        }

        // Update email
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    @Override
    public List<User> getNewUsers() {
        return userRepository.findNewUsers();
    }

    @Override
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}