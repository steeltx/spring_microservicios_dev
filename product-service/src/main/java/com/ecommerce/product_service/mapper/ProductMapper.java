package com.ecommerce.product_service.mapper;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;
import com.ecommerce.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequestDto requestDto);

    ProductResponseDto toProductResponseDto(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequestDto productRequestDto, @MappingTarget Product product);

}
