package com.ilich.sb.e_commerce.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.payload.ProductDTO;

@Mapper(componentModel = "spring")
public interface IProductMapper {

    IProductMapper INSTANCE = Mappers.getMapper(IProductMapper.class);

    @Mapping(source = "category", target = "categoryDTO")
    ProductDTO toDto(Product product);

    @Mapping(source = "categoryDTO", target = "category")
    Product toEntity(ProductDTO productDTO);
    
    @Mapping(source = "category", target = "categoryDTO")
    List<ProductDTO> toDtoList(List<Product> categories);

    @Mapping(source = "categoryDTO", target = "category")
    List<Product> toEntityList(List<ProductDTO> categoriesDTO);
}
