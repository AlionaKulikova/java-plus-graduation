package ewm.compilation.service;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.NewCompilationDto;
import ewm.compilation.dto.UpdateCompilationRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getAll(Boolean pinned, Pageable pageRequest);

    CompilationDto getBy(Long id);

    CompilationDto add(NewCompilationDto compilationDto);

    void deleteBy(long id);

    CompilationDto updateBy(long id, UpdateCompilationRequest compilationDto);
}
