package ewm.eventservice.category.service;

import ewm.interaction.dto.event.category.CategoryDto;
import ewm.interaction.dto.event.category.CategoryDtoNew;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryAdmin(CategoryDtoNew newCategoryDto);

    CategoryDto updateCategoryAdmin(CategoryDtoNew newCategoryDto, Long catId);

    void deleteCategoryAdmin(Long catId);

    CategoryDto getByIDCategoryPublic(Long catId);

    List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size);
}