package ewm.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.events.dto.EventShortDto;
import ewm.events.mapper.EventMapper;
import ewm.events.model.Event;
import ewm.compilation.dto.CompilationDtoRequest;
import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.model.Compilation;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "events", source = "eventDTOs")
    CompilationDtoResponse toCompilationDto(Compilation compilation, List<EventShortDto> eventDTOs);

    @Mapping(target = "events", source = "events")
    @Mapping(target = "id", ignore = true)
    Compilation toCompilation(CompilationDtoRequest compilationDtoRequest, Set<Event> events);
}