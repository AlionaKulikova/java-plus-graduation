package ewm.eventandadditional.compilation.service;

import ewm.interaction.dto.eventandadditional.compilation.CompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.NewCompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.UpdateCompilationRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getAll(Boolean pinned, Pageable pageRequest);

    CompilationDto getBy(Long id);

    CompilationDto add(NewCompilationDto compilationDto);

    void deleteBy(long id);

    CompilationDto updateBy(long id, UpdateCompilationRequest compilationDto);
}
