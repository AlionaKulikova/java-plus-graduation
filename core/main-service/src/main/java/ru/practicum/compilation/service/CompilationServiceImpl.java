package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventService eventService;
    private final EventMapper eventMapper;

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> eventList;
        if (newCompilationDto.getEvents() != null) {
            eventList = eventService.getAllEventsByListId(newCompilationDto.getEvents());
        } else {
            eventList = new ArrayList<>();
        }

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(eventList);
        compilation = compilationRepository.save(compilation);
        log.info("Добавлена новая подборка");

        return mapperEventsAndSetView(compilation, eventList);
    }

    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation with id = " + compId + " was not found"));
        compilationRepository.deleteById(compId);
        log.info("Подборка с id = {}, успешно удалена", compId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilationSaved = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation with id = " + compId + " was not found"));

        List<Event> eventList = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            eventList = eventService.getAllEventsByListId(updateCompilationRequest.getEvents());
            compilationSaved.setEvents(eventList);
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilationSaved.setIsPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilationSaved.setTitle(updateCompilationRequest.getTitle());
        }
        log.info("Подборка с ID = {} успешно обновлена.", compId);
        return mapperEventsAndSetView(compilationSaved, eventList);
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation with id = " + compId + " was not found"));
        List<Event> eventList = eventService.getAllEventsByListId(compilation.getEvents().stream()
                .map(Event::getId).collect(Collectors.toList()));
        log.info("Возвращен пользователь с id = {}", compId);
        return mapperEventsAndSetView(compilation, eventList);

    }

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = compilationRepository.findAllByIsPinned(pinned, PageRequest.of(from / size, size));

        List<CompilationDto> compilationDtoList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Event> eventList = eventService.getAllEventsByListId(compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList()));
            List<EventShortDto> eventShortDtoList = eventList.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
            CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
            compilationDto.setEvents(eventShortDtoList);
            compilationDtoList.add(compilationDto);
        }
        return compilationDtoList;
    }


    private CompilationDto mapperEventsAndSetView(Compilation compilation, List<Event> eventList) {
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        List<EventShortDto> eventsShortDto = eventList.stream().map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
        compilationDto.setEvents(eventsShortDto);
        return compilationDto;
    }
}
