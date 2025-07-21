package ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.category.dto.CategoryDto;
import ewm.category.dto.CategoryDtoNew;
import ewm.category.mapper.CategoryMapper;
import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.error.exception.ConflictException;
import ewm.error.exception.ResourceNotFoundException;
import ewm.events.model.Event;
import ewm.events.repository.EventRepository;

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
            throw new ResourceNotFoundException(Category.class, catId);
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
        findCategoryByIdOrThrow(catId);
        List<Event> events = eventRepository.findByCategoryId(catId);
        log.info("Найдено событий для категории {}: {}", catId, events.size());
        if (!events.isEmpty()) {
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
            throw new ResourceNotFoundException(Category.class, catId);
        }
    }
}