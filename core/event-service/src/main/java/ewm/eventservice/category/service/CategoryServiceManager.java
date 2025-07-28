package ewm.eventservice.category.service;

import ewm.eventservice.event.repository.EventRepository;
import ewm.interaction.dto.event.category.CategoryDto;
import ewm.interaction.dto.event.category.CategoryDtoNew;
import ewm.eventservice.category.mapper.CategoryMapper;
import ewm.eventservice.category.model.Category;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.eventservice.category.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceManager implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;//репозиторий событий

    @Override
    @Transactional
    public CategoryDto createCategoryAdmin(CategoryDtoNew categoryDtoNew) {
        if (categoryRepository.existsByName(categoryDtoNew.getName())) {
            throw new ConflictException("Категория с именем '" + categoryDtoNew.getName() + "' уже существует.");
        }
        log.info("Создание новой категории админом {}.", categoryDtoNew);
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(categoryDtoNew)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryAdmin(CategoryDtoNew categoryDtoNew, Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Ресурс не найден");
        }
        Optional<Category> existingCategory = categoryRepository.findByName(categoryDtoNew.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(catId)) {
            throw new ConflictException("Категория с именем '" + categoryDtoNew.getName() + "' уже существует.");
        }
        Category category = categoryMapper.toCategory(categoryDtoNew);
        category.setId(catId);
        log.info("Обновление админом категории с id {} на {}.", catId, categoryDtoNew);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Не найдено событий для категории с id" + catId));
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Невозможно удалить. В категории содержатся события.");
        }
        categoryRepository.deleteById(catId);
        log.info("Админ удалил категорию с id {}", catId);

    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getByIDCategoryPublic(Long catId) {
        CategoryDto categoryDto = categoryMapper.toCategoryDto(findCategoryByIdOrThrow(catId));
        log.info("Получение публичного доступа категории с id {}", catId);
        return categoryDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesPublic(Integer from, Integer size) {
        log.info("Получение публичного списка всех категорий from={}, size={}", from, size);
        return categoryMapper.toCategoryDto(categoryRepository.findAll(PageRequest.of(from / size, size)).toList());
    }

    private Category findCategoryByIdOrThrow(Long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isPresent()) {
            return category.get();
        } else {
            throw new NotFoundException("Ресурс не найден");
        }
    }
}