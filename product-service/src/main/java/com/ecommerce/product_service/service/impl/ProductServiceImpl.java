package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;
import com.ecommerce.product_service.mapper.ProductMapper;
import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = mapper.toProduct(requestDto);
        Product savedProduct = repository.save(product);
        return mapper.toProductResponseDto(savedProduct);
    }

    @Override
    public List<ProductResponseDto> getAllsProducts() {
        return repository.findAll()
                .stream()
                .map(mapper::toProductResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto getProductById(String id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el id: "+id));
        return mapper.toProductResponseDto(product);
    }

    @Override
    public ProductResponseDto updateProduct(String id, ProductRequestDto productRequestDto) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el id: "+id));
        mapper.updateProductFromRequest(productRequestDto, product);
        Product updatedProduct = repository.save(product);
        return mapper.toProductResponseDto(updatedProduct);
    }

    @Override
    public void deleteProduct(String id) {
        if(!repository.existsById(id)){
            throw new RuntimeException("Producto no encontrado con el id: "+id);
        }
        repository.deleteById(id);
    }
}
