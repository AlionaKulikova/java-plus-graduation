package ewm.eventservice.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.dto.ViewStatsDto;
import ewm.eventservice.category.model.Category;
import ewm.eventservice.category.model.QCategory;
import ewm.eventservice.event.mappers.EventMapper;
import ewm.eventservice.event.model.Event;
import ewm.eventservice.event.model.QEvent;
import ewm.eventservice.event.repository.EventRepository;
import ewm.eventservice.event.service.interfaces.EventServiceAdmin;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventState;
import ewm.interaction.dto.event.event.ParamsEventAdmin;
import ewm.interaction.dto.event.event.UpdateEventAdminRequest;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.ConflictException;
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
public class EventServiceAdminImpl implements EventServiceAdmin {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final JPAQueryFactory jpaQueryFactory;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    public List<EventFullDto> getEventsByParams(ParamsEventAdmin params, Pageable pageRequest) {
        BooleanBuilder eventQueryExpression = buildExpression(params);

        List<EventFullDto> events = getEvents(pageRequest, eventQueryExpression);
        List<Long> eventIds = events.stream().map(EventFullDto::getId).toList();
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        Set<String> uris = events.stream()
                .map(event -> "/events/" + event.getId()).collect(Collectors.toSet());

        LocalDateTime start = events
                .stream()
                .min(Comparator.comparing(EventFullDto::getEventDate))
                .orElseThrow(() -> new NotFoundException("Не заданы даты"))
                .getEventDate();

        List<EventFullDto> eventFullDto =   events.stream().peek(shortDto -> {
            //shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequests.getOrDefault(shortDto.getId(), 0L));
        }).toList();

        log.info("Получены события по параметрам для админа");
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByID(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));

        if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
            throw new ConflictException("Администратор может обновлять события только в состоянии ожидания публикации");
        }
        Category category = event.getCategory();
        eventRepository.save(eventMapper.toUpdatedEvent(event, dto, category));
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        log.info("Обновлено событие {} с id {} админом", eventFullDto, eventId);
        return eventFullDto;
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return requestFeignClient.getConfirmedRequests(eventIds);
    }

    private List<EventFullDto> getEvents(Pageable pageRequest, BooleanBuilder eventQueryExpression) {
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
                .map(event -> eventMapper.toEventFullDto(event, initiators.get(event.getInitiatorId())))
                .toList();
    }

    private BooleanBuilder buildExpression(ParamsEventAdmin params) {
        BooleanBuilder eventQueryExpression = new BooleanBuilder();

        QEvent qEvent = QEvent.event;
        Optional.ofNullable(params.getUserIds())
                .ifPresent(userIds -> eventQueryExpression.and(qEvent.initiatorId.in(userIds)));
        Optional.ofNullable(params.getStates())
                .ifPresent(userStates -> eventQueryExpression.and(qEvent.state.in(userStates)));
        Optional.ofNullable(params.getCategories())
                .ifPresent(categoryIds -> eventQueryExpression.and(qEvent.category.id.in(categoryIds)));
        Optional.ofNullable(params.getRangeStart())
                .ifPresent(rangeStart -> eventQueryExpression.and(qEvent.eventDate.after(rangeStart)));
        Optional.ofNullable(params.getRangeEnd())
                .ifPresent(rangeEnd -> eventQueryExpression.and(qEvent.eventDate.before(rangeEnd)));

        return eventQueryExpression;
    }
}