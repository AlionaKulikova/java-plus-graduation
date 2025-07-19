package ewm.category.service;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto create(NewCategoryDto categoryDto);

    void deleteBy(long id);

    CategoryDto updateBy(long id, NewCategoryDto newCategoryDto);
}
