package com.example.marketplace.controller;

import com.example.marketplace.dto.category.CategoryCreateRequest;
import com.example.marketplace.dto.category.CategoryDto;
import com.example.marketplace.dto.category.CategoryUpdateRequest;
import com.example.marketplace.model.product.Category;
import com.example.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.debug("REST request to get all Categories");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        log.debug("REST request to get all active Categories");
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        log.debug("REST request to get root Categories");
        List<Category> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        log.debug("REST request to get Category : {}", id);
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(convertToDto(category));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryDto>> getSubcategories(@PathVariable Long id) {
        log.debug("REST request to get subcategories for Category : {}", id);
        List<Category> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<CategoryDto>> getCategoryPath(@PathVariable Long id) {
        log.debug("REST request to get path for Category : {}", id);
        List<Category> path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(path.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryDto>> searchCategories(@RequestParam String keyword) {
        log.debug("REST request to search Categories with keyword: {}", keyword);
        List<Category> categories = categoryService.searchCategories(keyword);
        return ResponseEntity.ok(categories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/top")
    public ResponseEntity<List<CategoryDto>> getTopCategories(@RequestParam(defaultValue = "5") int limit) {
        log.debug("REST request to get top {} Categories", limit);
        List<Category> categories = categoryService.getTopCategories(limit);
        return ResponseEntity.ok(categories.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        log.debug("REST request to create Category : {}", request);

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());

        if (request.getParentId() != null) {
            Category parent = new Category();
            parent.setId(request.getParentId());
            category.setParent(parent);
        }

        Category result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        log.debug("REST request to update Category : {}, {}", id, request);

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setActive(request.getActive());

        if (request.getParentId() != null) {
            Category parent = new Category();
            parent.setId(request.getParentId());
            category.setParent(parent);
        }

        Category result = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(convertToDto(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.debug("REST request to delete Category : {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> toggleCategoryStatus(@PathVariable Long id) {
        log.debug("REST request to toggle status of Category : {}", id);
        categoryService.toggleCategoryStatus(id);
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(Map.of("active", category.getActive()));
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImage(category.getImage());
        dto.setActive(category.getActive());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }
}