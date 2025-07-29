package ewm.eventandadditional.category.service;


import ewm.interaction.dto.eventandadditional.category.CategoryDto;
import ewm.interaction.dto.eventandadditional.category.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto create(NewCategoryDto categoryDto);

    void deleteBy(long id);

    CategoryDto updateBy(long id, NewCategoryDto newCategoryDto);
}
