package ewm.eventservice.compilation.service.interfaces;

import ewm.interaction.dto.event.compilation.CompilationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompilationServicePublic {
    CompilationDto getCompilationByIdPublic(Long compId);

    List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Pageable pageRequest);
}