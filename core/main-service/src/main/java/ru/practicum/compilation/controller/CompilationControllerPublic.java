package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDtoResponse;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

import static ru.practicum.util.PathConstants.COMPILATIONS;
import static ru.practicum.util.PathConstants.COMPILATION_ID;

@RequestMapping(COMPILATIONS)
@RestController
@Slf4j
@RequiredArgsConstructor
public class CompilationControllerPublic {
    private final CompilationService compilationService;

    @GetMapping(COMPILATION_ID)
    public ResponseEntity<CompilationDtoResponse> getById(@PathVariable Long compId) {
        log.info("Эндпоинт /compilations/{}. GET запрос на получение(public) подборки с id {}.", compId, compId);
        return new ResponseEntity<CompilationDtoResponse>(compilationService.getCompilationByIdPublic(compId),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CompilationDtoResponse>> getAll(@RequestParam(required = false) Boolean pinned,
                                                               @RequestParam(required = false, defaultValue = "0")
                                                               @PositiveOrZero Integer from,
                                                               @RequestParam(required = false, defaultValue = "10")
                                                               @Positive Integer size) {
        log.info("Эндпоинт /compilations. GET запрос на получение(public) подборок с параметрами " +
                "pinned = {}, from = {}, size = {}.", pinned, from, size);
        return new ResponseEntity<>(compilationService.getAllCompilationsPublic(pinned, from, size), HttpStatus.OK);
    }
}
