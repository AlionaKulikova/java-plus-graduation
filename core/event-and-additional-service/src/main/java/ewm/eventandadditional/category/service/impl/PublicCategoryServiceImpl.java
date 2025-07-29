package ewm.eventandadditional.category.service.impl;

import ewm.eventandadditional.category.mapper.CategoryMapper;
import ewm.eventandadditional.category.repository.CategoryRepository;
import ewm.eventandadditional.category.service.PublicCategoryService;
import ewm.interaction.dto.eventandadditional.category.CategoryDto;
import ewm.interaction.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicCategoryServiceImpl implements PublicCategoryService {
    final CategoryRepository categoryRepository;
    final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAllBy(Pageable pageRequest) {
        return categoryRepository.findAll(pageRequest).map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getBy(long id) {
        return categoryRepository.findById(id).map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + id + " не найдена"));
    }
}
