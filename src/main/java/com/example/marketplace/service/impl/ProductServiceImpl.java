package com.example.marketplace.service.impl;

import com.example.marketplace.dto.PagedResponse;
import com.example.marketplace.dto.product.ProductCreateRequest;
import com.example.marketplace.dto.product.ProductDto;
import com.example.marketplace.dto.product.ProductSearchCriteria;
import com.example.marketplace.dto.product.ProductUpdateRequest;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.product.Category;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.store.Store;
import com.example.marketplace.repository.jpa.ProductRepository;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.CategoryService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.StoreService;
import com.example.marketplace.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final CategoryService categoryService;
    private final AuthService authService;

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
    }

    @Override
    public PagedResponse<ProductDto> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination");
        Page<Product> products = productRepository.findAll(pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByStore(Long storeId, Pageable pageable) {
        log.debug("Fetching products by store ID: {}", storeId);
        // Verify store exists
        storeService.getStoreById(storeId);

        Page<Product> products = productRepository.findByStoreId(storeId, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products by category ID: {}", categoryId);
        // Verify category exists
        categoryService.getCategoryById(categoryId);

        Page<Product> products = productRepository.findByCategories_Id(categoryId, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByCategoryName(String categoryName, Pageable pageable) {
        log.debug("Fetching products by category name: {}", categoryName);
        Page<Product> products = productRepository.findByCategoryName(categoryName, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> searchProducts(String keyword, Pageable pageable) {
        log.debug("Searching products with keyword: {}", keyword);
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByName(String name, Pageable pageable) {
        log.debug("Fetching products by name: {}", name);
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getFeaturedProducts(Pageable pageable) {
        log.debug("Fetching featured products");
        Page<Product> products = productRepository.findByFeaturedTrue(pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    public PagedResponse<ProductDto> getAvailableProducts(Pageable pageable) {
        log.debug("Fetching available products");
        Page<Product> products = productRepository.findByActiveTrueAndQuantityGreaterThan(0, pageable);
        return new PagedResponse<>(products.map(this::convertToDto));
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductCreateRequest request) {
        log.debug("Creating product: {}", request);

        // Determine the store ID
        Long storeId;
        if (authService.getCurrentUser().getRole().name().equals("ADMIN") && request.getStoreId() != null) {
            storeId = request.getStoreId();
        } else {
            // Get the current user's store
            Store store = storeService.getStoreByOwnerId(authService.getCurrentUser().getId());
            storeId = store.getId();
        }

        Store store = storeService.getStoreById(storeId);

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImages((Set<String>) request.getImages());
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        product.setActive(true);
        product.setStore(store);

        // Add categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {}", savedProduct.getId());

        return convertToDto(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProduct(Long id, ProductUpdateRequest request) {
        log.debug("Updating product ID: {} with: {}", id, request);
        Product product = getProductById(id);

        // Update product fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            product.setImages((Set<String>) request.getImages());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        // Update categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);

        return convertToDto(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.debug("Deleting product ID: {}", id);
        // Verify product exists
        getProductById(id);

        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void toggleProductStatus(Long id) {
        log.debug("Toggling status for product ID: {}", id);
        Product product = getProductById(id);
        product.setActive(!product.getActive());
        productRepository.save(product);
        log.info("Product status toggled: ID={}, newStatus={}", id, product.getActive());
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void updateProductStock(Long id, Integer quantityDelta) {
        log.debug("Updating stock for product ID: {} by {}", id, quantityDelta);
        Product product = getProductById(id);

        int newQuantity = product.getQuantity() + quantityDelta;
        if (newQuantity < 0) {
            log.warn("Cannot reduce stock below zero for product ID: {}", id);
            throw new IllegalArgumentException("Cannot reduce stock below zero");
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);
        log.info("Product stock updated: ID={}, newQuantity={}", id, newQuantity);
    }

    @Override
    public List<ProductDto> getTopSellingProducts(int limit) {
        log.debug("Fetching top {} selling products", limit);
        List<Product> products = productRepository.findTopSellingProducts(limit);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getNewProducts() {
        log.debug("Fetching new products");
        List<Product> products = productRepository.findNewProducts();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isProductAvailable(Long id, Integer requestedQuantity) {
        log.debug("Checking if product ID: {} is available in quantity: {}", id, requestedQuantity);
        Product product = getProductById(id);
        return product.getActive() && product.getQuantity() >= requestedQuantity;
    }

    @Override
    public PagedResponse<ProductDto> findProducts(ProductSearchCriteria criteria) {
        log.debug("Finding products with criteria: {}", criteria);

        Pageable pageable = PaginationUtil.getPageable(criteria);

        // Utiliser la méthode de recherche multiple
        Page<Product> productPage = productRepository.findProductsByMultipleCriteria(
                criteria.getCategoryId(),
                criteria.getStoreId(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getFeatured(),
                criteria.getInStock() != null && criteria.getInStock() ? true : null,
                criteria.getKeyword(),
                pageable);

        Page<ProductDto> dtoPage = productPage.map(this::convertToDto);

        return new PagedResponse<>(dtoPage);
    }

    private ProductDto convertToDto(Product product) {
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
                    .map(Category::getId)
                    .collect(Collectors.toList()));

            dto.setCategoryNames(product.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}