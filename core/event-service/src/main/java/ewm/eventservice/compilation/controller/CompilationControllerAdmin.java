package ewm.eventservice.compilation.controller;

import ewm.eventservice.compilation.service.interfaces.CompilationServiceAdmin;
import ewm.interaction.dto.event.compilation.CompilationDto;
import ewm.interaction.dto.event.compilation.NewCompilationDto;
import ewm.interaction.dto.event.compilation.UpdateCompilationRequest;
import ewm.interaction.util.PathConstants;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(PathConstants.ADMIN_COMPILATIONS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationControllerAdmin {
    private final CompilationServiceAdmin compilationServiceAdmin;

    @PostMapping
    public ResponseEntity<CompilationDto> create(@Valid @RequestBody NewCompilationDto compilationRequestDto) {
        log.info("Эндпоинт /admin/compilations. POST запрос на создание админом новой подборки {}.",
                compilationRequestDto);
        return new ResponseEntity<>(compilationServiceAdmin.createCompilationAdmin(compilationRequestDto),
                HttpStatus.CREATED);
    }

    @PatchMapping(PathConstants.COMPILATION_ID)
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto update(@PathVariable("compId") Long compId,
                                 @Valid @RequestBody UpdateCompilationRequest compilationUpdateDto) {
        log.info("Эндпоинт /admin/compilations/{}. PATCH запрос на обновление админом подборки на {}.",
                compId, compilationUpdateDto);
        return compilationServiceAdmin.updateCompilationAdmin(compId, compilationUpdateDto);
    }

    @DeleteMapping(PathConstants.COMPILATION_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("compId") Long compId) {
        compilationServiceAdmin.deleteCompilationAdmin(compId);
        log.info("Эндпоинт /admin/compilations/{}. DELETE запрос  на удаление админом подборки с id {}.", compId,
                compId);
    }
}