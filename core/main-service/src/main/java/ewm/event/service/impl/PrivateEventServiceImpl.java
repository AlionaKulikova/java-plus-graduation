package ewm.event.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.category.mapper.CategoryMapper;
import ewm.category.model.Category;
import ewm.category.model.QCategory;
import ewm.category.service.PublicCategoryService;
import ewm.client.StatRestClientImpl;
import ewm.dto.ViewStatsDto;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.event.mappers.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.QEvent;
import ewm.event.repository.EventRepository;
import ewm.event.service.PrivateEventService;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.PermissionException;
import ewm.request.model.QParticipationRequest;
import ewm.request.model.RequestStatus;
import ewm.user.mappers.UserMapper;
import ewm.user.model.QUser;
import ewm.user.model.User;
import ewm.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrivateEventServiceImpl implements PrivateEventService {
    final UserService userService;
    final PublicCategoryService categoryService;
    final EventRepository eventRepository;
    final UserMapper userMapper;
    final CategoryMapper categoryMapper;
    final EventMapper eventMapper;
    final StatRestClientImpl statRestClient;
    final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<EventShortDto> getAllBy(long userId, Pageable pageRequest) {
        BooleanExpression booleanExpression = QEvent.event.initiator.id.eq(userId);
        List<EventShortDto> events = getEvents(pageRequest, booleanExpression);
        List<Long> eventIds = events.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);

        Set<String> uris = events.stream()
                .map(event -> "/events/" + event.getId()).collect(Collectors.toSet());

        LocalDateTime start = events
                .stream()
                .min(Comparator.comparing(EventShortDto::getEventDate))
                .orElseThrow(() -> new NotFoundException("Даты не заданы"))
                .getEventDate();

        Map<String, Long> viewMap = statRestClient
                .stats(start, LocalDateTime.now(), uris.stream().toList(), false).stream()
                .collect(Collectors.groupingBy(ViewStatsDto::getUri, Collectors.summingLong(ViewStatsDto::getHits)));

        return events.stream().peek(shortDto -> {
            shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(shortDto.getId(), 0L));
        }).toList();
    }

    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto newEventDto) {
        User initiator = userMapper.toUser(userService.findBy(userId));
        Category category = categoryMapper.toCategory(categoryService.getBy(newEventDto.getCategory()));
        Event event = eventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto getBy(long userId, long eventId) {
        EventFullDto eventFullDto = eventRepository.findById(eventId).map(eventMapper::toEventFullDto)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (eventFullDto.getInitiator().getId() != userId) {
            throw new PermissionException("Доступ запрещен");
        }
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateBy(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с с id = " + eventId + " не найдено"));
        if (event.getInitiator().getId() != userId) {
            throw new PermissionException("Доступ запрещен");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя отменить событие с состоянием");
        }
        Category category = categoryMapper.toCategory(categoryService.getBy(event.getCategory().getId()));
        return eventMapper.toEventFullDto(eventMapper.toUpdatedEvent(event, updateEventUserRequest, category));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        QParticipationRequest qRequest = QParticipationRequest.participationRequest;

        return jpaQueryFactory
                .select(qRequest.event.id.as("eventId"), qRequest.count().as("confirmedRequests"))
                .from(qRequest)
                .where(qRequest.event.id.in(eventIds).and(qRequest.status.eq(RequestStatus.CONFIRMED)))
                .groupBy(qRequest.event.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> Optional.ofNullable(tuple.get(1, Long.class)).orElse(0L))
                );
    }

    private List<EventShortDto> getEvents(Pageable pageRequest, BooleanExpression eventQueryExpression) {
        return jpaQueryFactory
                .selectFrom(QEvent.event)
                .leftJoin(QEvent.event.category, QCategory.category)
                .fetchJoin()
                .leftJoin(QEvent.event.initiator, QUser.user)
                .fetchJoin()
                .where(eventQueryExpression)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }
}
