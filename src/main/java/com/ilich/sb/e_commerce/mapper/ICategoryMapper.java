package com.ilich.sb.e_commerce.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.payload.CategoryDTO;

@Mapper(componentModel = "spring")
public interface ICategoryMapper {

    ICategoryMapper INSTANCE = Mappers.getMapper(ICategoryMapper.class);

    CategoryDTO toDto(Category category);

    Category toEntity(CategoryDTO categoryDTO);

    List<CategoryDTO> toDtoList(List<Category> categories);

    List<Category> toEntityList(List<CategoryDTO> categoriesDTO);
}