package ewm.category.service;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.CategoryDtoNew;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryAdmin(CategoryDtoNew newCategoryDto);

    CategoryDto updateCategoryAdmin(CategoryDtoNew newCategoryDto, Long catId);

    void deleteCategoryAdmin(Long catId);

    CategoryDto getByIDCategoryPublic(Long catId);

    List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size);
}