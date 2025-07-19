package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryDtoRequest;
import ru.practicum.category.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDtoRequest categoryDtoRequest);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDtoRequest categoryDtoRequest);

    List<CategoryDto> getAllCategories(Pageable pageable);

    CategoryDto getCategoryById(Long catsId);

    Category getCategoryByIdNotMapping(Long catId);
}
