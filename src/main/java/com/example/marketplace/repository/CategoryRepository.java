package com.example.marketplace.repository;

import com.example.marketplace.model.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIsNull();

    List<Category> findByActiveTrue();

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Category> searchCategories(@Param("keyword") String keyword);

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT id, name, parent_id, 1 as level" +
            "  FROM categories" +
            "  WHERE id = :categoryId" +
            "  UNION ALL" +
            "  SELECT c.id, c.name, c.parent_id, ct.level + 1" +
            "  FROM categories c" +
            "  JOIN category_tree ct ON c.parent_id = ct.id" +
            ")" +
            "SELECT * FROM category_tree ORDER BY level DESC",
            nativeQuery = true)
    List<Object[]> findCategoryPathById(@Param("categoryId") Long categoryId);

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT id, name, description, parent_id, 0 as level" +
            "  FROM categories" +
            "  WHERE parent_id IS NULL" +
            "  UNION ALL" +
            "  SELECT c.id, c.name, c.description, c.parent_id, ct.level + 1" +
            "  FROM categories c" +
            "  JOIN category_tree ct ON c.parent_id = ct.id" +
            ")" +
            "SELECT * FROM category_tree ORDER BY level, name",
            nativeQuery = true)
    List<Object[]> findCategoryHierarchy();

    @Query(value = "SELECT c.* FROM categories c " +
            "JOIN product_categories pc ON c.id = pc.category_id " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(pc.product_id) DESC LIMIT :limit",
            nativeQuery = true)
    List<Category> findTopCategories(@Param("limit") int limit);
}