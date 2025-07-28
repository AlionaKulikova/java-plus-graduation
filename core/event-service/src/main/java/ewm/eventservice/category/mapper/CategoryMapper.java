package ewm.eventservice.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.interaction.dto.event.category.CategoryDto;
import ewm.interaction.dto.event.category.CategoryDtoNew;
import ewm.eventservice.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryDtoNew categoryDtoNew);

    Category toCategory(CategoryDto categoryDtoNew);

    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDto(List<Category> categories);
}