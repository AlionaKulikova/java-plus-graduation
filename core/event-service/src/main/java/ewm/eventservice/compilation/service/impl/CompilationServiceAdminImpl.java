package ewm.eventservice.compilation.service.impl;

import ewm.eventservice.compilation.mappers.CompilationMapper;
import ewm.eventservice.compilation.model.Compilation;
import ewm.eventservice.compilation.repository.CompilationRepository;
import ewm.eventservice.compilation.service.interfaces.CompilationServiceAdmin;
import ewm.eventservice.event.model.Event;
import ewm.eventservice.event.repository.EventRepository;
import ewm.interaction.dto.event.compilation.CompilationDto;
import ewm.interaction.dto.event.compilation.NewCompilationDto;
import ewm.interaction.dto.event.compilation.UpdateCompilationRequest;
import ewm.interaction.exception.NotFoundException;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceAdminImpl implements CompilationServiceAdmin {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilationAdmin(NewCompilationDto compilationRequestDto) {
        if (compilationRequestDto.getTitle() == null || compilationRequestDto.getTitle().isBlank()) {
            throw new ValidationException("Поле title не может быть пустым.");
        }
        List<Event> events = eventRepository.findAllByIdIn(compilationRequestDto.getEvents());
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilationRepository
                .save(compilationMapper.toCompilation(compilationRequestDto, events)));

        log.info("Создана новая подборка {}", compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationAdmin(Long compId, UpdateCompilationRequest compilationUpdateDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID=" + compId + " не найдена"));

        List<Event> events = eventRepository.findAllByIdIn(compilationUpdateDto.getEvents());

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilationRepository
                .save(compilationMapper.toUpdateCompilation(compilation, compilationUpdateDto, events)));

        log.info("Обновлена подборка с id {} на {}", compId, compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(Long compId) {
        compilationRepository.deleteById(compId);
        log.info("Удалена подборка с id {}.", compId);
    }
}