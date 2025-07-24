package ewm.eventservice.event.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.client.StatRestClientImpl;
import ewm.dto.ViewStatsDto;
import ewm.eventservice.category.mapper.CategoryMapper;
import ewm.eventservice.category.mapper.User;
import ewm.eventservice.category.mapper.UserMapper;
import ewm.eventservice.category.model.Category;
import ewm.eventservice.category.model.QCategory;
import ewm.eventservice.category.service.CategoryService;
import ewm.eventservice.event.mappers.EventMapper;
import ewm.eventservice.event.model.Event;
import ewm.eventservice.event.model.QEvent;
import ewm.eventservice.event.repository.EventRepository;
import ewm.eventservice.event.service.interfaces.EventServicePrivate;
import ewm.interaction.dto.event.event.*;
import ewm.interaction.dto.user.UserDto;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServicePrivateImpl implements EventServicePrivate {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final JPAQueryFactory jpaQueryFactory;
    private final StatRestClientImpl statRestClient;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;
    private final UserMapper userMapper;

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        EventFullDto eventFullDto = eventRepository.findById(eventId).map(eventMapper::toEventFullDto)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));
        UserDto userDto = userFeignClient.getUsers(List.of(userId), 0, 10).getFirst();
        eventFullDto.setInitiator(userMapper.toUserShortDto(userDto));

        if (eventFullDto.getInitiator().getId() != userId) {
            throw new ConflictException(
                    "Пользователь с id=" + userId + " не является владельцем события с id=" + eventId);
        }

        log.info("Получено событие с id {} пользователя с id {}", eventId, userId);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userMapper.toUser(userFeignClient.getUsers(List.of(userId), 0, 10).getFirst());
        Category category = categoryMapper.toCategory(categoryService.getByIDCategoryPublic(newEventDto.getCategory()));
        Event event = eventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setInitiator(userMapper.toUserShortDto(initiator));

        log.info("Создано событие {} пользователем с id {}", eventFullDto, userId);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Pageable pageRequest) {
        BooleanExpression booleanExpression = QEvent.event.initiatorId.eq(userId);
        List<EventShortDto> events = getEvents(pageRequest, booleanExpression);
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

        List<EventShortDto> eventShortDto = events.stream().peek(shortDto -> {
            shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(shortDto.getId(), 0L));
        }).toList();

        log.info("Получили список событий по заданным параметрам");
        return eventShortDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));

        if (event.getInitiatorId() != userId) {
            throw new ConflictException(
                    "Пользователь с id=" + userId + " не является владельцем события с id=" + eventId);
        }
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Изменять можно только события в состоянии PENDING или CANCELLED");
        }

        Category category = categoryMapper
                .toCategory(categoryService.getByIDCategoryPublic(event.getCategory().getId()));

        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventMapper
                .toUpdatedEvent(event, updateRequest, category));

        log.info("Обновлено событие {} пользователем с id {}", eventFullDto, userId);
        return eventFullDto;
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return requestFeignClient.getConfirmedRequests(eventIds);
    }

    private List<EventShortDto> getEvents(Pageable pageRequest, BooleanExpression eventQueryExpression) {
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
}