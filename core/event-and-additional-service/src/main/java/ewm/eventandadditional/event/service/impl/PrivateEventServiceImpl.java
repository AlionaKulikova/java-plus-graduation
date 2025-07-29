package ewm.eventandadditional.event.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.eventandadditional.category.mapper.CategoryMapper;
import ewm.eventandadditional.category.mapper.User;
import ewm.eventandadditional.category.mapper.UserMapper;
import ewm.eventandadditional.category.model.Category;
import ewm.eventandadditional.category.model.QCategory;
import ewm.eventandadditional.category.service.PublicCategoryService;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.eventandadditional.event.model.Event;
import ewm.eventandadditional.event.model.QEvent;
import ewm.eventandadditional.event.repository.EventRepository;
import ewm.eventandadditional.event.service.PrivateEventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.EventState;
import ewm.interaction.dto.eventandadditional.event.NewEventDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventUserRequest;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.PermissionException;
import ewm.interaction.feign.RequestFeignClient;
import ewm.interaction.feign.UserFeignClient;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrivateEventServiceImpl implements PrivateEventService {
    final UserFeignClient userFeignClient;
    final PublicCategoryService categoryService;
    final EventRepository eventRepository;
    final UserMapper userMapper;
    final CategoryMapper categoryMapper;
    final EventMapper eventMapper;
    final JPAQueryFactory jpaQueryFactory;
    final RequestFeignClient requestFeignClient;

    @Override
    public List<EventShortDto> getAllBy(long userId, Pageable pageRequest) {
        BooleanExpression booleanExpression = QEvent.event.initiatorId.eq(userId);
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

//        Map<String, Long> viewMap = statRestClient
//                .stats(start, LocalDateTime.now(), uris.stream().toList(), false).stream()
//                .collect(Collectors.groupingBy(ViewStatsDto::getUri, Collectors.summingLong(ViewStatsDto::getHits)));

        return events.stream().peek(shortDto -> {
            //shortDto.setViews(viewMap.getOrDefault("/events/" + shortDto.getId(), 0L));
            shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(shortDto.getId(), 0L));
        }).toList();
    }

    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto newEventDto) {
        User initiator = userMapper.toUser(userFeignClient.findAllBy(List.of(userId), 0, 10).getFirst());
        Category category = categoryMapper.toCategory(categoryService.getBy(newEventDto.getCategory()));
        Event event = eventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setInitiator(userMapper.toUserShortDto(initiator));
        return eventFullDto;
    }

    @Override
    public EventFullDto getBy(long userId, long eventId) {
        EventFullDto eventFullDto = eventRepository.findById(eventId).map(eventMapper::toEventFullDto)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        UserDto userDto = userFeignClient.findAllBy(List.of(userId), 0, 10).getFirst();
        eventFullDto.setInitiator(userMapper.toUserShortDto(userDto));
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
        if (event.getInitiatorId() != userId) {
            throw new PermissionException("Доступ запрещен");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя отменить событие с состоянием");
        }
        Category category = categoryMapper.toCategory(categoryService.getBy(event.getCategory().getId()));
        return eventMapper.toEventFullDto(eventMapper.toUpdatedEvent(event, updateEventUserRequest, category));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        return requestFeignClient.getConfirmedRequestMap(eventIds);
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
        Map<Long, UserShortDto> initiators = userFeignClient.userMapBy(initiatorIds);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, initiators.get(event.getInitiatorId())))
                .toList();


    }
}
