package com.example.marketplace.service;

import com.example.marketplace.model.user.User;
import com.example.marketplace.model.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    User getUserByEmail(String email);

    Page<User> getAllUsers(Pageable pageable);

    Page<User> getUsersByRole(UserRole role, Pageable pageable);

    Page<User> searchUsers(String keyword, Pageable pageable);

    User createUser(User user);

    User updateUser(Long id, User userDetails);

    void deleteUser(Long id);

    void changeUserRole(Long id, UserRole newRole);

    void updatePassword(Long id, String currentPassword, String newPassword);

    void updateEmail(Long id, String newEmail, String password);

    void toggleUserStatus(Long id);

    List<User> getNewUsers();

    long countByRole(UserRole role);
}