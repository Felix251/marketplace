package com.example.marketplace.service;

import com.example.marketplace.model.product.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    Category getCategoryById(Long id);

    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    List<Category> getRootCategories();

    List<Category> getSubcategories(Long parentId);

    List<Category> getActiveCategories();

    List<Category> searchCategories(String keyword);

    Category createCategory(Category category);

    Category updateCategory(Long id, Category categoryDetails);

    void deleteCategory(Long id);

    void toggleCategoryStatus(Long id);

    List<Category> getTopCategories(int limit);

    List<Category> getCategoryPath(Long categoryId);

    Map<Category, List<Category>> getCategoryHierarchy();
}