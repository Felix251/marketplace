package com.example.marketplace.repository;

import com.example.marketplace.model.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByOwnerId(Long ownerId);

    Page<Store> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Store> findByActiveTrue();

    @Query("SELECT s FROM Store s WHERE s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<Store> searchStores(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM stores s ORDER BY "
            + "(SELECT COUNT(*) FROM orders o JOIN order_items oi ON o.id = oi.order_id "
            + "JOIN products p ON oi.product_id = p.id "
            + "WHERE p.store_id = s.id) DESC LIMIT :limit",
            nativeQuery = true)
    List<Store> findTopStoresByOrderCount(@Param("limit") int limit);

    @Query(value = "SELECT * FROM stores s WHERE s.created_at > current_date - interval '30 day'",
            nativeQuery = true)
    List<Store> findNewStores();
}