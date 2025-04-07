package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.product.Category;
import com.example.marketplace.repository.jpa.CategoryRepository;
import com.example.marketplace.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = "categories", key = "#id")
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Cacheable(value = "categories", key = "'root'")
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public List<Category> getSubcategories(Long parentId) {
        // Verify parent exists
        getCategoryById(parentId);

        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Override
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.searchCategories(keyword);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Check parent if specified
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = getCategoryById(category.getParent().getId());
            category.setParent(parent);
        }

        // Set active by default
        if (category.getActive() == null) {
            category.setActive(true);
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);

        // Update category fields
        if (categoryDetails.getName() != null) {
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getDescription() != null) {
            category.setDescription(categoryDetails.getDescription());
        }
        if (categoryDetails.getImage() != null) {
            category.setImage(categoryDetails.getImage());
        }
        if (categoryDetails.getActive() != null) {
            category.setActive(categoryDetails.getActive());
        }

        // Update parent if specified
        if (categoryDetails.getParent() != null) {
            // Prevent cyclic hierarchy
            if (categoryDetails.getParent().getId() != null &&
                    isDescendantOfCategory(categoryDetails.getParent().getId(), id)) {
                throw new IllegalArgumentException("Cannot set a descendant category as parent");
            }

            if (categoryDetails.getParent().getId() != null) {
                Category parent = getCategoryById(categoryDetails.getParent().getId());
                category.setParent(parent);
            } else {
                category.setParent(null); // Setting as root category
            }
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // If has subcategories, move them to parent or make them root
        List<Category> subcategories = categoryRepository.findByParentId(id);
        for (Category subcategory : subcategories) {
            subcategory.setParent(category.getParent()); // Move to grandparent or make root
            categoryRepository.save(subcategory);
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", key = "#id")
    public void toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setActive(!category.getActive());
        categoryRepository.save(category);
    }

    @Override
    public List<Category> getTopCategories(int limit) {
        return categoryRepository.findTopCategories(limit);
    }

    @Override
    @Cacheable(value = "categoryPaths", key = "#categoryId")
    public List<Category> getCategoryPath(Long categoryId) {
        List<Object[]> path = categoryRepository.findCategoryPathById(categoryId);
        List<Category> result = new ArrayList<>();

        // Convert Object[] results to Category objects
        for (Object[] row : path) {
            Category category = new Category();
            category.setId(Long.valueOf(row[0].toString()));
            category.setName((String) row[1]);
            // Add other fields if needed
            result.add(category);
        }

        return result;
    }

    @Override
    @Cacheable(value = "categoryHierarchy")
    public Map<Category, List<Category>> getCategoryHierarchy() {
        List<Object[]> hierarchyData = categoryRepository.findCategoryHierarchy();
        Map<Long, Category> categoriesById = new HashMap<>();
        Map<Category, List<Category>> hierarchy = new HashMap<>();

        // First pass: create all Category objects
        for (Object[] row : hierarchyData) {
            Long id = Long.valueOf(row[0].toString());
            String name = (String) row[1];
            String description = (String) row[2];
            Long parentId = row[3] != null ? Long.valueOf(row[3].toString()) : null;
            Integer level = (Integer) row[4];

            Category category = new Category();
            category.setId(id);
            category.setName(name);
            category.setDescription(description);

            categoriesById.put(id, category);
            hierarchy.put(category, new ArrayList<>());
        }

        // Second pass: build parent-child relationships
        for (Object[] row : hierarchyData) {
            Long id = Long.valueOf(row[0].toString());
            Long parentId = row[3] != null ? Long.valueOf(row[3].toString()) : null;

            if (parentId != null) {
                Category child = categoriesById.get(id);
                Category parent = categoriesById.get(parentId);

                if (parent != null && child != null) {
                    hierarchy.get(parent).add(child);
                }
            }
        }

        return hierarchy;
    }

    private boolean isDescendantOfCategory(Long potentialDescendantId, Long ancestorId) {
        Category current = getCategoryById(potentialDescendantId);

        while (current.getParent() != null) {
            if (current.getParent().getId().equals(ancestorId)) {
                return true;
            }
            current = getCategoryById(current.getParent().getId());
        }

        return false;
    }
}