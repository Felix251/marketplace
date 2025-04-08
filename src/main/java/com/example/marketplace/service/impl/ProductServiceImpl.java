package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.product.Category;
import com.example.marketplace.model.product.Product;
import com.example.marketplace.model.store.Store;
import com.example.marketplace.repository.jpa.ProductRepository;
import com.example.marketplace.service.CategoryService;
import com.example.marketplace.service.ProductService;
import com.example.marketplace.service.StoreService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final CategoryService categoryService;

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getProductsByStore(Long storeId, Pageable pageable) {
        // Verify store exists
        storeService.getStoreById(storeId);

        return productRepository.findByStoreId(storeId, pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        // Verify category exists
        categoryService.getCategoryById(categoryId);

        return productRepository.findByCategories_Id(categoryId, pageable);
    }

    @Override
    public Page<Product> getProductsByCategoryName(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryName(categoryName, pageable);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public Page<Product> getProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByFeaturedTrue(pageable);
    }

    @Override
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findByActiveTrueAndQuantityGreaterThan(0, pageable);
    }

    @Override
    @Transactional
    public Product createProduct(Long storeId, Product product, List<Long> categoryIds) {
        Store store = storeService.getStoreById(storeId);
        product.setStore(store);

        // Set default values
        if (product.getActive() == null) {
            product.setActive(true);
        }
        if (product.getFeatured() == null) {
            product.setFeatured(false);
        }

        // Add categories
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : categoryIds) {
                Category category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(Long id, Product productDetails, List<Long> categoryIds) {
        Product product = getProductById(id);

        // Update product fields
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getQuantity() != null) {
            product.setQuantity(productDetails.getQuantity());
        }
        if (productDetails.getImages() != null && !productDetails.getImages().isEmpty()) {
            product.setImages(productDetails.getImages());
        }
        if (productDetails.getFeatured() != null) {
            product.setFeatured(productDetails.getFeatured());
        }
        if (productDetails.getActive() != null) {
            product.setActive(productDetails.getActive());
        }

        // Update categories if provided
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : categoryIds) {
                Category category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        // Verify product exists
        getProductById(id);

        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void toggleProductStatus(Long id) {
        Product product = getProductById(id);
        product.setActive(!product.getActive());
        productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void updateProductStock(Long id, Integer quantityDelta) {
        Product product = getProductById(id);

        int newQuantity = product.getQuantity() + quantityDelta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero");
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);
    }

    @Override
    public List<Product> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(limit);
    }

    @Override
    public List<Product> getNewProducts() {
        return productRepository.findNewProducts();
    }

    @Override
    public boolean isProductAvailable(Long id, Integer requestedQuantity) {
        Product product = getProductById(id);
        return product.getActive() && product.getQuantity() >= requestedQuantity;
    }
}