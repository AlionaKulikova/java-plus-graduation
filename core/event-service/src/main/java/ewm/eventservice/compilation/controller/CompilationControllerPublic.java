package ewm.eventservice.compilation.controller;

import ewm.eventservice.compilation.service.interfaces.CompilationServicePublic;
import ewm.interaction.dto.event.compilation.CompilationDto;
import ewm.interaction.util.PathConstants;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(PathConstants.COMPILATIONS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationControllerPublic {
    private final CompilationServicePublic compilationServicePublic;

    @GetMapping(PathConstants.COMPILATION_ID)
    public ResponseEntity<CompilationDto> getById(@PositiveOrZero @PathVariable Long compId) {
        log.info("Эндпоинт /compilations/{}. GET запрос на получение(public) подборки с id {}.", compId, compId);
        return new ResponseEntity<CompilationDto>(compilationServicePublic.getCompilationByIdPublic(compId),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAll(@RequestParam(required = false) Boolean pinned,
                                                       @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                       @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Эндпоинт /compilations. GET запрос на получение(public) подборок с параметрами " +
                "pinned = {}, from = {}, size = {}.", pinned, from, size);
        return new ResponseEntity<>(compilationServicePublic.getAllCompilationsPublic(pinned, PageRequest.of(from, size)
        ), HttpStatus.OK);
    }
}