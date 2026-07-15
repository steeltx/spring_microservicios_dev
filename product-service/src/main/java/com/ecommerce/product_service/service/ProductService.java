package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto requestDto);
    List<ProductResponseDto> getAllsProducts();
    ProductResponseDto getProductById(String id);
    ProductResponseDto updateProduct(String id, ProductRequestDto productRequestDto);
    void deleteProduct(String id);
}
