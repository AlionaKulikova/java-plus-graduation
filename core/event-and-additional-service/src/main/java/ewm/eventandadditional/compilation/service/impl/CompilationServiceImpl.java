package ewm.eventandadditional.compilation.service.impl;

import com.querydsl.core.BooleanBuilder;
import ewm.eventandadditional.category.mapper.UserMapper;
import ewm.eventandadditional.compilation.mappers.CompilationMapper;
import ewm.eventandadditional.compilation.model.Compilation;
import ewm.eventandadditional.compilation.model.QCompilation;
import ewm.eventandadditional.compilation.repository.CompilationRepository;
import ewm.eventandadditional.compilation.service.CompilationService;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.eventandadditional.event.model.Event;
import ewm.eventandadditional.event.repository.EventRepository;
import ewm.interaction.dto.eventandadditional.compilation.CompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.NewCompilationDto;
import ewm.interaction.dto.eventandadditional.compilation.UpdateCompilationRequest;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.ValidationException;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final CompilationMapper compilationMapper;
    final EventRepository eventRepository;
    final UserFeignClient userFeignClient;
    final EventMapper eventMapper;
    final UserMapper userMapper;

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageRequest) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (pinned != null) {
            booleanBuilder.and(QCompilation.compilation.pinned.eq(pinned));
        }

        List<Compilation> compilations = compilationRepository
                .findAll(booleanBuilder, pageRequest)
                .toList();

        List<CompilationDto> compilationDtoListToReturn = new ArrayList<>();
        for (int i = 0; i < compilations.size(); i++) {
            CompilationDto compilationDto = compilationMapper.toCompilationDto(compilations.get(i));
            List<Event> currentEvents = compilations.get(i).getEvents().stream().toList();
            List<EventShortDto> eventShortDtoList = new ArrayList<>();
            for (int j = 0; j < currentEvents.size(); j++) {
                UserShortDto initiatorShortDto = userMapper
                        .toUserShortDto(userFeignClient
                                .findAllBy(List.of(currentEvents.get(j).getInitiatorId()), 0, 10).getFirst());
                EventShortDto eventShortDto = eventMapper
                        .toEventShortDto(currentEvents.get(j), initiatorShortDto);
                eventShortDtoList.add(eventShortDto);
            }
            compilationDto.setEvents(new HashSet<>(eventShortDtoList));
            compilationDtoListToReturn.add(compilationDto);
        }

        return compilationDtoListToReturn;
    }

    @Override
    public CompilationDto getBy(Long id) {
        Optional<Compilation> compilationOptional = compilationRepository.findById(id);
        if (compilationOptional.isEmpty()) {
            throw new NotFoundException("Подборка с id=" + id + " не найдена");
        }
        return compilationMapper.toCompilationDto(compilationOptional.get());
    }

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto compilationDto) {
        if (compilationDto.getTitle() == null || compilationDto.getTitle().isBlank()) {
            throw new ValidationException("Поле title не может быть пустой или состоять из пробела");
        }

        List<Event> events = eventRepository.findAllByIdIn(compilationDto.getEvents());
        return compilationMapper.toCompilationDto(compilationRepository
                .save(compilationMapper.toCompilation(compilationDto, events)));
    }

    @Override
    @Transactional
    public void deleteBy(long id) {
        compilationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CompilationDto updateBy(long id, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не найдено"));

        List<Event> events = eventRepository.findAllByIdIn(compilationDto.getEvents());
        return compilationMapper.toCompilationDto(compilationRepository
                .save(compilationMapper.toUpdateCompilation(compilation, compilationDto, events)));
    }
}
