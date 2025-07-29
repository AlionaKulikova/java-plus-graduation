package ewm.eventandadditional.category.service.impl;

import ewm.eventandadditional.category.mapper.CategoryMapper;
import ewm.eventandadditional.category.model.Category;
import ewm.eventandadditional.category.repository.CategoryRepository;
import ewm.eventandadditional.category.service.AdminCategoryService;
import ewm.eventandadditional.event.repository.EventRepository;
import ewm.interaction.dto.eventandadditional.category.CategoryDto;
import ewm.interaction.dto.eventandadditional.category.NewCategoryDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCategoryServiceImpl implements AdminCategoryService {
    final CategoryRepository categoryRepository;
    final CategoryMapper categoryMapper;
    final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto categoryDto) {
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(categoryDto)));
    }

    @Override
    public void deleteBy(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категории с id = " + id + " не существует"));
        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("Обьект имеет зависимость с событием");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto updateBy(long id, NewCategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        category.setName(categoryDto.getName());

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}

