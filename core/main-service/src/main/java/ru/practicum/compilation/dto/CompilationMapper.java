package ru.practicum.compilation.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.model.Compilation;

@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "pinned", target = "isPinned")
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(source = "isPinned", target = "pinned")
    @Mapping(target = "events", ignore = true)
    CompilationDto toCompilationDto(Compilation compilation);
}
