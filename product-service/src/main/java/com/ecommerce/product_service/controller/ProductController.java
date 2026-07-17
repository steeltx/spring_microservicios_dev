package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;
import com.ecommerce.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto createProduct(@RequestBody @Valid ProductRequestDto productRequestDto){
        return productService.createProduct(productRequestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDto> getAllProducts(){
        return productService.getAllsProducts();
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable String id){
        return productService.getProductById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDto update(@PathVariable String id, @RequestBody @Valid ProductRequestDto productRequestDto){
        return productService.updateProduct(id, productRequestDto);
    }

    @GetMapping("/test-fail")
    public void testFail(){
        throw new RuntimeException("Error simulado");
    }

}
