package com.example.marketplace.repository;

import com.example.marketplace.model.user.User;
import com.example.marketplace.model.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    List<User> findByEnabledTrue();

    @Query(value = "SELECT * FROM users u WHERE u.created_at > current_date - interval '30 day'", nativeQuery = true)
    List<User> findNewUsers();
}