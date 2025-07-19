package ewm.category.service;

import ewm.category.dto.CategoryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublicCategoryService {
    List<CategoryDto> findAllBy(Pageable pageRequest);

    CategoryDto getBy(long id);
}
