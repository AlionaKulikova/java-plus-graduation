package ewm.eventservice.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.client.StatRestClientImpl;
import ewm.dto.ViewStatsDto;
import ewm.eventservice.category.model.QCategory;
import ewm.eventservice.event.mappers.EventMapper;
import ewm.eventservice.event.model.Event;
import ewm.eventservice.event.model.QEvent;
import ewm.eventservice.event.repository.EventRepository;
import ewm.eventservice.event.service.interfaces.EventServicePublic;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.event.event.EventState;
import ewm.interaction.dto.event.event.ParamsEventPublic;
import ewm.interaction.dto.request.RequestStatus;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.RequestFeignClient;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServicePublicImpl implements EventServicePublic {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;
    private final StatRestClientImpl statRestClient;
    private final JPAQueryFactory jpaQueryFactory;

    private static final int TIME_BEFORE = 10;

    @Override
    public List<EventShortDto> getEventsByParams(ParamsEventPublic params, Pageable pageRequest) {
        BooleanBuilder eventQueryExpression = buildExpression(params);
        List<EventShortDto> events = getEvents(pageRequest, eventQueryExpression);
        List<Long> eventIds = events.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(eventIds);

        Set<String> uris = events.stream()
                .map(event -> "/events/" + event.getId()).collect(Collectors.toSet());

        LocalDateTime start = events
                .stream()
                .min(Comparator.comparing(EventShortDto::getEventDate))
                .orElseThrow(() -> new NotFoundException("Не заданы даты"))
                .getEventDate();

        Map<String, Long> viewMap = statRestClient
                .stats(start, LocalDateTime.now(), uris.stream().toList(), false).stream()
                .collect(Collectors.groupingBy(ViewStatsDto::getUri, Collectors.summingLong(ViewStatsDto::getHits)));

        List<EventShortDto> eventsShortDto = events.stream().peek(shortDto -> {
            shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(shortDto.getId(), 0L));
        }).toList();

        log.info("Получены события по заданным параметрам");
        return eventsShortDto;
    }

    @Override
    public EventFullDto getEventByID(Long eventId) {
        EventFullDto event = eventRepository.findById(eventId).map(eventMapper::toEventFullDto)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusYears(TIME_BEFORE);

        statRestClient.stats(start, now, List.of("/events/" + eventId), true)
                .forEach(viewStatsDto -> event.setViews(viewStatsDto.getHits()));

        long confirmedRequests = requestFeignClient.countAllByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED.toString());
        event.setConfirmedRequests(confirmedRequests);

        log.info("Получено событие {} найденное по id события {}", event, eventId);
        return event;
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return requestFeignClient.getConfirmedRequests(eventIds);
    }

    private List<EventShortDto> getEvents(Pageable pageRequest, BooleanBuilder eventQueryExpression) {
        List<Event> events = jpaQueryFactory
                .selectFrom(QEvent.event)
                .leftJoin(QEvent.event.category, QCategory.category)
                .fetchJoin()
                .where(eventQueryExpression)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .stream()
                .toList();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        Map<Long, UserShortDto> initiators = userFeignClient.getUsersByIDS(initiatorIds);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, initiators.get(event.getInitiatorId())))
                .toList();
    }

    private BooleanBuilder buildExpression(ParamsEventPublic params) {
        BooleanBuilder eventQueryExpression = new BooleanBuilder();

        eventQueryExpression.and(QEvent.event.state.eq(EventState.PUBLISHED));
        Optional.ofNullable(params.getRangeStart())
                .ifPresent(rangeStart -> eventQueryExpression.and(QEvent.event.eventDate.after(rangeStart)));
        Optional.ofNullable(params.getRangeEnd())
                .ifPresent(rangeEnd -> eventQueryExpression.and(QEvent.event.eventDate.before(params.getRangeEnd())));
        Optional.ofNullable(params.getPaid())
                .ifPresent(paid -> eventQueryExpression.and(QEvent.event.paid.eq(paid)));
        Optional.ofNullable(params.getCategories())
                .filter(category -> !category.isEmpty())
                .ifPresent(category -> eventQueryExpression.and(QEvent.event.category.id.in(category)));
        Optional.ofNullable(params.getText())
                .filter(text -> !text.isEmpty()).ifPresent(text -> {
                    eventQueryExpression.and(QEvent.event.annotation.containsIgnoreCase(text));
                    eventQueryExpression.or(QEvent.event.description.containsIgnoreCase(text));
                });

        return eventQueryExpression;
    }
}