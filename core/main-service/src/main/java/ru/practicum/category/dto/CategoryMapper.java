package ru.practicum.category.dto;

import org.mapstruct.Mapper;
import ru.practicum.category.model.Category;

@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    Category toCategory(CategoryDtoRequest categoryDtoRequest);

    CategoryDto toCategoryDto(Category category);
}
