package ewm.eventandadditional.category.mapper;

import ewm.eventandadditional.category.model.Category;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.interaction.dto.eventandadditional.category.CategoryDto;
import ewm.interaction.dto.eventandadditional.category.NewCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class})
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    Category toCategory(CategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);
}
