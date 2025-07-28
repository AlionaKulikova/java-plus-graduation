package ewm.eventservice.compilation.service.impl;

import com.querydsl.core.BooleanBuilder;
import ewm.eventservice.category.mapper.UserMapper;
import ewm.eventservice.compilation.mappers.CompilationMapper;
import ewm.eventservice.compilation.model.Compilation;
import ewm.eventservice.compilation.model.QCompilation;
import ewm.eventservice.compilation.repository.CompilationRepository;
import ewm.eventservice.compilation.service.interfaces.CompilationServicePublic;
import ewm.eventservice.event.mappers.EventMapper;
import ewm.eventservice.event.model.Event;
import ewm.interaction.dto.event.compilation.CompilationDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServicePublicImpl implements CompilationServicePublic {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final UserFeignClient userFeignClient;
    private final UserMapper userMapper;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto getCompilationByIdPublic(Long compId) {
        Optional<Compilation> compilationOptional = compilationRepository.findById(compId);

        if (compilationOptional.isEmpty()) {
            throw new NotFoundException("Подборка с ID=" + compId + " не найдена");
        }
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilationOptional.get());

        log.info("Получили подборку с id {}.", compId);
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Pageable pageRequest) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (pinned != null) {
            booleanBuilder.and(QCompilation.compilation.pinned.eq(pinned));
        }
        List<Compilation> compilations = compilationRepository
                .findAll(booleanBuilder, pageRequest)
                .toList();
        List<CompilationDto> compilationDtoListToReturn = new ArrayList<>();

        for (Compilation compilation : compilations) {
            CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
            List<Event> currentEvents = compilation.getEvents().stream().toList();
            List<EventShortDto> eventShortDtoList = new ArrayList<>();
            for (Event currentEvent : currentEvents) {
                UserShortDto initiatorShortDto = userMapper
                        .toUserShortDto(userFeignClient
                                .getUsers(List.of(currentEvent.getInitiatorId()), 0, 10).getFirst());
                EventShortDto eventShortDto = eventMapper
                        .toEventShortDto(currentEvent, initiatorShortDto);
                eventShortDtoList.add(eventShortDto);
            }
            compilationDto.setEvents(new HashSet<>(eventShortDtoList));
            compilationDtoListToReturn.add(compilationDto);
        }

        log.info("Получили подбороки по заданным параметрам");
        return compilationDtoListToReturn;
    }
}