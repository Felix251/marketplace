package com.example.marketplace.repository.jpa;

import com.example.marketplace.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategories_Id(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    Page<Product> findByFeaturedTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndQuantityGreaterThan(int minQuantity, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.name = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM products p " +
            "JOIN (SELECT product_id, COUNT(*) as order_count FROM order_items GROUP BY product_id) oi " +
            "ON p.id = oi.product_id " +
            "ORDER BY oi.order_count DESC LIMIT :limit",
            nativeQuery = true)
    List<Product> findTopSellingProducts(@Param("limit") int limit);

    @Query(value = "SELECT * FROM products WHERE created_at > current_date - interval '30 day' ORDER BY created_at DESC",
            nativeQuery = true)
    List<Product> findNewProducts();
}