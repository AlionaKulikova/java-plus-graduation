package ewm.eventservice.compilation.service.interfaces;

import ewm.interaction.dto.event.compilation.CompilationDto;
import ewm.interaction.dto.event.compilation.NewCompilationDto;
import ewm.interaction.dto.event.compilation.UpdateCompilationRequest;

public interface CompilationServiceAdmin {
    CompilationDto createCompilationAdmin(NewCompilationDto compilationRequestDto);

    CompilationDto updateCompilationAdmin(Long compId, UpdateCompilationRequest compilationUpdateDto);

    void deleteCompilationAdmin(Long compId);

}