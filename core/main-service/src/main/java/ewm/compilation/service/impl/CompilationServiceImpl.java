package ewm.compilation.service.impl;

import com.querydsl.core.BooleanBuilder;
import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.NewCompilationDto;
import ewm.compilation.dto.UpdateCompilationRequest;
import ewm.compilation.mappers.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.compilation.model.QCompilation;
import ewm.compilation.repository.CompilationRepository;
import ewm.compilation.service.CompilationService;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageRequest) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (pinned != null) {
            booleanBuilder.and(QCompilation.compilation.pinned.eq(pinned));
        }

        return compilationRepository.findAll(booleanBuilder, pageRequest)
                .stream().map(compilationMapper::toCompilationDto)
                .toList();
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
