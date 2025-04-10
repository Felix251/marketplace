package com.example.marketplace.controller;

import com.example.marketplace.dto.PagedResponse;
import com.example.marketplace.dto.product.ProductCreateRequest;
import com.example.marketplace.dto.product.ProductDto;
import com.example.marketplace.dto.product.ProductSearchCriteria;
import com.example.marketplace.dto.product.ProductUpdateRequest;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.StoreService;
import com.example.marketplace.util.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final StoreService storeService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductDto>> getAllProducts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        log.debug("REST request to get all Products");
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        log.debug("REST request to get Product : {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(convertToProductDto(product));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByStore(
            @PathVariable Long storeId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get Products by store ID: {}", storeId);
        return ResponseEntity.ok(productService.getProductsByStore(storeId, pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get Products by category ID: {}", categoryId);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/category-name/{categoryName}")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByCategoryName(
            @PathVariable String categoryName,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get Products by category name: {}", categoryName);
        return ResponseEntity.ok(productService.getProductsByCategoryName(categoryName, pageable));
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<PagedResponse<ProductDto>> searchProductsByKeyword(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to search Products with keyword: {}", keyword);
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/search/name")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByName(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get Products by name: {}", name);
        return ResponseEntity.ok(productService.getProductsByName(name, pageable));
    }

    @GetMapping("/search/price")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get Products by price range: {} - {}", minPrice, maxPrice);
        return ResponseEntity.ok(productService.getProductsByPriceRange(minPrice, maxPrice, pageable));
    }

    @GetMapping("/featured")
    public ResponseEntity<PagedResponse<ProductDto>> getFeaturedProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get featured Products");
        return ResponseEntity.ok(productService.getFeaturedProducts(pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<PagedResponse<ProductDto>> getAvailableProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("REST request to get available Products");
        return ResponseEntity.ok(productService.getAvailableProducts(pageable));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductDto>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        log.debug("REST request to get top {} selling Products", limit);
        return ResponseEntity.ok(productService.getTopSellingProducts(limit));
    }

    @GetMapping("/new")
    public ResponseEntity<List<ProductDto>> getNewProducts() {
        log.debug("REST request to get new Products");
        return ResponseEntity.ok(productService.getNewProducts());
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<Map<String, Boolean>> isProductAvailable(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity) {
        log.debug("REST request to check if Product {} is available in quantity {}", id, quantity);
        boolean isAvailable = productService.isProductAvailable(id, quantity);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductDto>> findProducts(ProductSearchCriteria criteria) {
        log.debug("REST request to find products with criteria: {}", criteria);
        return ResponseEntity.ok(productService.findProducts(criteria));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.debug("REST request to create Product : {}", request);

        // Vérifie que l'utilisateur est un vendeur ou un admin
        if (!isSellerOrAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductDto result = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.debug("REST request to update Product : {}, {}", id, request);

        // Vérifie que l'utilisateur est le propriétaire du produit ou un admin
        if (!canManageProduct(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductDto result = productService.updateProduct(id, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("REST request to delete Product : {}", id);

        // Vérifie que l'utilisateur est le propriétaire du produit ou un admin
        if (!canManageProduct(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> toggleProductStatus(@PathVariable Long id) {
        log.debug("REST request to toggle status of Product : {}", id);

        // Vérifie que l'utilisateur est le propriétaire du produit ou un admin
        if (!canManageProduct(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.toggleProductStatus(id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(Map.of("active", product.getActive()));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, Integer>> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer quantityDelta) {
        log.debug("REST request to update stock of Product : {} by {}", id, quantityDelta);

        // Vérifie que l'utilisateur est le propriétaire du produit ou un admin
        if (!canManageProduct(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.updateProductStock(id, quantityDelta);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(Map.of("quantity", product.getQuantity()));
    }

    // Helper methods

    private boolean isSellerOrAdmin() {
        String role = authService.getCurrentUser().getRole().name();
        return "SELLER".equals(role) || "ADMIN".equals(role);
    }

    private boolean canManageProduct(Long productId) {
        Product product = productService.getProductById(productId);
        Long currentUserId = authService.getCurrentUser().getId();
        boolean isAdmin = "ADMIN".equals(authService.getCurrentUser().getRole().name());
        boolean isStoreOwner = storeService.isUserStoreOwner(currentUserId, product.getStore().getId());

        return isAdmin || isStoreOwner;
    }

    private ProductDto convertToProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setImages((List<String>) product.getImages());
        dto.setFeatured(product.getFeatured());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());

        if (product.getStore() != null) {
            dto.setStoreId(product.getStore().getId());
            dto.setStoreName(product.getStore().getName());
        }

        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategoryIds(product.getCategories().stream()
                    .map(category -> category.getId())
                    .collect(Collectors.toList()));

            dto.setCategoryNames(product.getCategories().stream()
                    .map(category -> category.getName())
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}