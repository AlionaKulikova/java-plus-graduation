package ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.compilation.dto.CompilationDtoUpdate;
import ewm.error.exception.ResourceNotFoundException;
import ewm.events.model.Event;
import ewm.events.dto.EventShortDto;
import ewm.events.mapper.EventMapper;
import ewm.events.repository.EventRepository;
import ewm.compilation.dto.CompilationDtoRequest;
import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.compilation.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceManager implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDtoResponse createCompilationAdmin(CompilationDtoRequest compilationDtoRequest) {
        if (compilationDtoRequest.getPinned() == null) {
            compilationDtoRequest.setPinned(false);
        }
        Set<Event> events = convertListToSet(eventRepository.findEventsByIdIn(compilationDtoRequest.getEvents()));
        validateEventCount(events.size(), compilationDtoRequest.getEvents().size());
        Compilation compilation = compilationRepository.save(compilationMapper.toCompilation(compilationDtoRequest,
                events));

        log.info("Создание новой подборки {}", compilationDtoRequest);
        return compilationMapper.toCompilationDto(compilation, mapEventsToShortDto(events));
    }

    @Override
    @Transactional
    public CompilationDtoResponse updateCompilationAdmin(CompilationDtoUpdate compilationDtoRequest, Long compId) {
        Compilation compilation = getCompilation(compId);

        if (compilationDtoRequest.getPinned() != null) {
            compilation.setPinned(compilationDtoRequest.getPinned());
        }
        if (compilationDtoRequest.getEvents() != null) {
            Set<Event> events = convertListToSet(eventRepository.findEventsByIdIn(compilationDtoRequest.getEvents()));
            validateEventCount(events.size(), compilationDtoRequest.getEvents().size());
            compilation.setEvents(events);
        }
        if (compilationDtoRequest.getTitle() != null) {
            compilation.setTitle(compilationDtoRequest.getTitle());
        }

        log.info("Обновление подборки с id {} на {}", compId, compilationDtoRequest);
        return compilationMapper.toCompilationDto(compilation, mapEventsToShortDto(compilation.getEvents()));
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(Long compId) {
        findCompilationById(compId);
        log.info("Удаление подборки с id {}.", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDtoResponse getCompilationByIdPublic(Long compId) {
        Compilation compilation = getCompilation(compId);
        Set<Event> events = compilation.getEvents();
        CompilationDtoResponse compilationDtoResponse = compilationMapper.toCompilationDto(compilation,
                mapEventsToShortDto(events));

        log.info("Получение подборки с id {}.", compId);
        return compilationDtoResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDtoResponse> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllByPinnedIs(pinned, PageRequest.of(from / size, size));
        } else {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size)).toList();
        }

        log.info("Получение подборок с параметрами pinned={}, from={}, size={}", pinned, from, size);
        List<CompilationDtoResponse> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            result.add(compilationMapper.toCompilationDto(compilation, mapEventsToShortDto(compilation.getEvents())));
        }

        return result;
    }

    private void findCompilationById(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new ResourceNotFoundException(Compilation.class, compId);
        }
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findCompilationById(compId)
                .orElseThrow(() -> new ResourceNotFoundException(Compilation.class, compId));
    }

    private Set<Event> convertListToSet(List<Event> events) {
        return new HashSet<>(events);
    }

    private void validateEventCount(Integer existingCount, Integer requiredCount) {
        if (!existingCount.equals(requiredCount)) {
            throw new ResourceNotFoundException("Не найдено нужное количество подборок.");
        }
    }

    private List<EventShortDto> mapEventsToShortDto(Set<Event> events) {
        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(eventMapper.toEventShortDto(event));
        }

        return result;
    }
}