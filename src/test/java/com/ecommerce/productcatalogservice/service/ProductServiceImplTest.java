package com.ecommerce.productcatalogservice.service;

import com.ecommerce.productcatalogservice.dto.ProductResponse;
import com.ecommerce.productcatalogservice.entity.Product;
import com.ecommerce.productcatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.productcatalogservice.exception.ProductNotFoundException;

import com.ecommerce.productcatalogservice.dto.ProductRequest;
import com.ecommerce.productcatalogservice.exception.DuplicateSkuException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldGetProductById() {

        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST-001");
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setCurrency("INR");
        product.setCategory("Electronics");
        product.setStockQuantity(10);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TEST-001", response.getSku());
        assertEquals("Test Product", response.getName());

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        verify(productRepository).findById(999L);
    }

    @Test
    void shouldCreateProduct() {

        ProductRequest request = new ProductRequest();
        request.setSku("TEST-002");
        request.setName("New Test Product");
        request.setPrice(new BigDecimal("2000"));
        request.setCurrency("INR");
        request.setCategory("Electronics");
        request.setStockQuantity(15);

        Product savedProduct = new Product();
        savedProduct.setId(2L);
        savedProduct.setSku("TEST-002");
        savedProduct.setName("New Test Product");
        savedProduct.setPrice(new BigDecimal("2000"));
        savedProduct.setCurrency("INR");
        savedProduct.setCategory("Electronics");
        savedProduct.setStockQuantity(15);

        when(productRepository.existsBySku("TEST-002"))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("TEST-002", response.getSku());
        assertEquals("New Test Product", response.getName());

        verify(productRepository).existsBySku("TEST-002");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSkuAlreadyExists() {

        ProductRequest request = new ProductRequest();
        request.setSku("TEST-001");
        request.setName("Duplicate Product");
        request.setPrice(new BigDecimal("1500"));
        request.setCurrency("INR");
        request.setCategory("Electronics");
        request.setStockQuantity(10);

        when(productRepository.existsBySku("TEST-001"))
                .thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository).existsBySku("TEST-001");

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() {

        ProductRequest request = new ProductRequest();
        request.setSku("TEST-001");
        request.setName("Updated Product");
        request.setPrice(new BigDecimal("2500"));
        request.setCurrency("INR");
        request.setCategory("Electronics");
        request.setStockQuantity(20);

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setSku("TEST-001");
        existingProduct.setName("Old Product");
        existingProduct.setPrice(new BigDecimal("2000"));
        existingProduct.setCurrency("INR");
        existingProduct.setCategory("Electronics");
        existingProduct.setStockQuantity(10);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenReturn(existingProduct);

        ProductResponse response =
                productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals("Updated Product", response.getName());
        assertEquals(new BigDecimal("2500"), response.getPrice());
        assertEquals(20, response.getStockQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void shouldDeleteProduct() {

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setSku("TEST-001");
        existingProduct.setName("Test Product");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(existingProduct);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingProduct() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        verify(productRepository).findById(999L);

        verify(productRepository, never())
                .delete(any(Product.class));
    }

    @Test
    void shouldSearchProducts() {

        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST-001");
        product.setName("Test Laptop");
        product.setPrice(new BigDecimal("50000"));
        product.setCurrency("INR");
        product.setCategory("Electronics");
        product.setStockQuantity(10);

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        PageRequest pageable = PageRequest.of(0, 10);

        when(productRepository.searchProducts(
                "Electronics",
                null,
                null,
                new BigDecimal("60000"),
                pageable
        )).thenReturn(productPage);

        Page<ProductResponse> response =
                productService.getAllProducts(
                        "Electronics",
                        null,
                        null,
                        new BigDecimal("60000"),
                        pageable
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("TEST-001", response.getContent().get(0).getSku());

        verify(productRepository).searchProducts(
                "Electronics",
                null,
                null,
                new BigDecimal("60000"),
                pageable
        );
    }
}