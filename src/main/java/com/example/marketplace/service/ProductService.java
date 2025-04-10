package com.example.marketplace.service;

import com.example.marketplace.dto.PagedResponse;
import com.example.marketplace.dto.product.ProductCreateRequest;
import com.example.marketplace.dto.product.ProductDto;
import com.example.marketplace.dto.product.ProductSearchCriteria;
import com.example.marketplace.dto.product.ProductUpdateRequest;
import com.example.marketplace.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Product getProductById(Long id);

    PagedResponse<ProductDto> getAllProducts(Pageable pageable);

    PagedResponse<ProductDto> getProductsByStore(Long storeId, Pageable pageable);

    PagedResponse<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable);

    PagedResponse<ProductDto> getProductsByCategoryName(String categoryName, Pageable pageable);

    PagedResponse<ProductDto> searchProducts(String keyword, Pageable pageable);

    PagedResponse<ProductDto> getProductsByName(String name, Pageable pageable);

    PagedResponse<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    PagedResponse<ProductDto> getFeaturedProducts(Pageable pageable);

    PagedResponse<ProductDto> getAvailableProducts(Pageable pageable);

    ProductDto createProduct(ProductCreateRequest request);

    ProductDto updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    void toggleProductStatus(Long id);

    void updateProductStock(Long id, Integer quantityDelta);

    List<ProductDto> getTopSellingProducts(int limit);

    List<ProductDto> getNewProducts();

    boolean isProductAvailable(Long id, Integer requestedQuantity);

    PagedResponse<ProductDto> findProducts(ProductSearchCriteria criteria);
}