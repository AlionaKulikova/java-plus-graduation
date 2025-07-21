package ewm.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.category.dto.CategoryDto;
import ewm.category.dto.CategoryDtoNew;
import ewm.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryDtoNew categoryDtoNew);

    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDto(List<Category> categories);
}