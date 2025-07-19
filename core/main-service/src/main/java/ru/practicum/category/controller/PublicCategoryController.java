package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/categories")
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Calling the GET request to /categories endpoint");
        Pageable pageable = PageRequest.of(from / size, size);
        List<CategoryDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long catId) {
        log.info("Calling the GET request to /categories/{catId} endpoint");
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryById(catId));
    }
}
