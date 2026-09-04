package com.ecommerce.productcatalogservice.service;

import com.ecommerce.productcatalogservice.dto.ProductRequest;
import com.ecommerce.productcatalogservice.dto.ProductResponse;
import com.ecommerce.productcatalogservice.entity.Product;
import com.ecommerce.productcatalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ecommerce.productcatalogservice.exception.ProductNotFoundException;
import com.ecommerce.productcatalogservice.exception.DuplicateSkuException;

import java.math.BigDecimal;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(
                    "Product with SKU already exists: " + request.getSku()
            );
        }

        Product product = new Product();

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setCurrency(request.getCurrency());
        product.setCategory(request.getCategory());
        product.setSubcategory(request.getSubcategory());
        product.setTags(request.getTags());
        product.setImageUrls(request.getImageUrls());
        product.setStockQuantity(request.getStockQuantity());

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with id: " + id)
                );

        return mapToResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return productRepository.searchProducts(
                        category,
                        brand,
                        minPrice,
                        maxPrice,
                        pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with id: " + id)
                );

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setCurrency(request.getCurrency());
        product.setCategory(request.getCategory());
        product.setSubcategory(request.getSubcategory());
        product.setTags(request.getTags());
        product.setImageUrls(request.getImageUrls());
        product.setStockQuantity(request.getStockQuantity());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with id: " + id)
                );

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getSalePrice(),
                product.getCurrency(),
                product.getCategory(),
                product.getSubcategory(),
                product.getTags(),
                product.getImageUrls(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}