package ewm.compilation.service;

import ewm.compilation.dto.CompilationDtoRequest;
import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.dto.CompilationDtoUpdate;

import java.util.List;

public interface CompilationService {
    CompilationDtoResponse createCompilationAdmin(CompilationDtoRequest compilationDtoRequest);

    CompilationDtoResponse updateCompilationAdmin(CompilationDtoUpdate compilationDtoRequest, Long compId);

    void deleteCompilationAdmin(Long compId);

    CompilationDtoResponse getCompilationByIdPublic(Long compId);

    List<CompilationDtoResponse> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size);
}