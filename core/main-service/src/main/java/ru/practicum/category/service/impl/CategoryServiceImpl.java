package ru.practicum.category.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryDtoRequest;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DataConflictRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.service.EventService;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventService eventService;

    @Transactional
    public CategoryDto createCategory(CategoryDtoRequest categoryDtoRequest) {
        log.info("Attempting to create new category: {}", categoryDtoRequest);

        String categoryName = categoryDtoRequest.getName();
        if (categoryRepository.existsByName(categoryName)) {
            log.warn("Category with name '{}' already exists", categoryName);
            throw new ConflictException("Category with name '" + categoryName + "' already exists");
        }

        Category category = categoryMapper.toCategory(categoryDtoRequest);
        Category savedCategory = categoryRepository.save(category);
        log.info("Created new category: {}", savedCategory);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            throw new NotFoundException("Category with id = " + catId + " was not found");
        } else if (eventService.findByCategory(category.get()).isPresent()) {
            throw new DataConflictRequest("Events are associated with the id = " + catId + " category");
        } else {
            categoryRepository.deleteById(catId);
            log.info("Категория с id = {}, успешно удалена", catId);
        }
    }

    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDtoRequest categoryDtoRequest) {
        log.info("Updating category with id: {}, new data: {}", catId, categoryDtoRequest);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id = " + catId + " was not found"));

        String newName = categoryDtoRequest.getName();
        if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            log.warn("Attempt to change category name to an existing one: {}", newName);
            throw new ConflictException("Category with name '" + newName + "' already exists");
        }

        category.setName(newName);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully: {}", updatedCategory);
        return categoryMapper.toCategoryDto(updatedCategory);
    }

    public List<CategoryDto> getAllCategories(Pageable pageable) {
        List<Category> categories;
        categories = categoryRepository.findAll(pageable).getContent();
        log.info("Список категорий успешно выдан");
        return categories.stream().map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long catId) {
        log.info("Поиск категории с id = {}", catId);
        return categoryMapper.toCategoryDto(getCategoryByIdNotMapping(catId));
    }

    public Category getCategoryByIdNotMapping(Long catId) {
        Optional<Category> categoryOptional = categoryRepository.findById(catId);
        if (categoryOptional.isPresent()) {
            return categoryOptional.get();
        }
        throw new NotFoundException("Category with id = " + catId + " was not found");
    }
}
