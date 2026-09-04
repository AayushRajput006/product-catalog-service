package com.ecommerce.productcatalogservice.service;

import com.ecommerce.productcatalogservice.dto.ProductRequest;
import com.ecommerce.productcatalogservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getAllProducts(
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}