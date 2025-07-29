package ewm.eventandadditional.compilation.controller;

import ewm.eventandadditional.compilation.service.CompilationService;
import ewm.interaction.dto.eventandadditional.compilation.CompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.NewCompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.UpdateCompilationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationController {
    final CompilationService compilationService;

    @GetMapping("/compilations")
    public List<CompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                       @Positive @RequestParam(defaultValue = "10") int size) {
        return compilationService.getAll(pinned, PageRequest.of(from, size));
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getBy(@PositiveOrZero @PathVariable long compId) {
        return compilationService.getBy(compId);
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto add(@Valid @RequestBody NewCompilationDto compilationDto) {
        return compilationService.add(compilationDto);
    }

    @DeleteMapping("/admin/compilations/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBy(@PathVariable("comId") long id) {
        compilationService.deleteBy(id);
    }

    @PatchMapping("/admin/compilations/{comId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateBy(@PathVariable("comId") long id,
                                   @Valid @RequestBody UpdateCompilationRequest compilationDto) {
        return compilationService.updateBy(id, compilationDto);
    }
}
