package ewm.eventandadditional.category.controller;

import ewm.eventandadditional.category.service.PublicCategoryService;
import ewm.interaction.dto.eventandadditional.category.CategoryDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicCategoryController {
    final PublicCategoryService categoryService;

    @GetMapping
    public List<CategoryDto> findAllBy(@RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return categoryService.findAllBy(PageRequest.of(from, size));
    }

    @GetMapping("/{catId}")
    public CategoryDto findBy(@PathVariable("catId") long catId) {
        return categoryService.getBy(catId);
    }
}
