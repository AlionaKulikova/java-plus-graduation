package ru.practicum.events.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.events.dto.*;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.mapper.LocationMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventSort;
import ru.practicum.events.model.EventState;
import ru.practicum.events.model.Location;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.stat.client.MainStatsClient;
import ru.practicum.subscriptions.model.Subscription;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class EventServiceImpl implements EventService {

    private final RequestRepository requestRepository;

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final MainStatsClient statClient;
    private final CategoryRepository categoryRepository;
    private final LocationMapper locationMapper;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @Transactional
    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        EndpointHit statDto = new EndpointHit(
                null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(FORMATTER)
        );
        log.info("Статистика: {}", statDto);
        statClient.createStats(statDto);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Поток был прерван во время ожидания", e);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ViewStats> stats = statClient.getStats(
                event.getPublishedOn().format(formatter),
                LocalDateTime.now().format(formatter),
                List.of("/events/" + id).toArray(new String[0]),
                true
        );

        long views = stats.isEmpty() ? 0 : stats.getFirst().getHits();
        long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests((int) confirmedRequests);
        eventRepository.save(event);

        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, int from, int size,
                                               HttpServletRequest request) {
        EndpointHit statDto = new EndpointHit(
                null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(FORMATTER)
        );
        statClient.createStats(statDto);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Начало диапазона не может быть позже его конца");
        }

        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                String pattern = "%%" + text.toLowerCase() + "%%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            if (onlyAvailable != null && onlyAvailable) {
                predicates.add(criteriaBuilder.greaterThan(root.get("participantLimit"), 0));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        EventSort eventSort = sort != null ? EventSort.valueOf(sort.toUpperCase()) : null;
        Sort sorting = Sort.unsorted();
        if (eventSort != null) {
            if (eventSort == EventSort.EVENT_DATE) {
                sorting = Sort.by(Sort.Direction.DESC, "eventDate");
            } else if (eventSort == EventSort.VIEWS) {
                sorting = Sort.by(Sort.Direction.DESC, "views");
            }
        }

        Pageable pageable = PageRequest.of(from / size, size, sorting);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public List<EventFullDto> getAdminEventById(List<Long> userIds, List<String> states, List<Long> categories,
                                                String rangeStart, String rangeEnd, Long from, Long size) {
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userIds != null && !userIds.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(userIds));
            }
            if (states != null && !states.isEmpty()) {
                List<EventState> eventStates = states.stream()
                        .map(EventState::valueOf)
                        .toList();
                predicates.add(root.get("state").in(eventStates));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (rangeStart != null && !rangeStart.isBlank()) {
                LocalDateTime startDate = LocalDateTime.parse(rangeStart, FORMATTER);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), startDate));
            }

            if (rangeEnd != null && !rangeEnd.isBlank()) {
                LocalDateTime endDate = LocalDateTime.parse(rangeEnd, FORMATTER);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(from.intValue() / size.intValue(), size.intValue());
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto dto) {
        Event event = checkEventExists(eventId);

        if (event.getState() != EventState.PENDING && event.getState() != EventState.PUBLISHED) {
            throw new ConflictException(
                    "Администратор может обновлять только события в состоянии PENDING или PUBLISHED");
        }

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            Category category = checkCategoryExists(dto.getCategory());
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            LocalDateTime eventDateTime = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
            LocalDateTime now = LocalDateTime.now();

            if (eventDateTime.isBefore(now)) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }

            if (event.getState() == EventState.PUBLISHED && event.getPublishedOn() != null) {
                LocalDateTime minAllowedDate = event.getPublishedOn().plusHours(1);
                if (eventDateTime.isBefore(minAllowedDate)) {
                    throw new ConflictException("Дата события должна быть не ранее чем через час после публикации");
                }
            }

            event.setEventDate(eventDateTime);
        }
        if (dto.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toEntity(dto.getLocation()));
            event.setLocation(location);
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new ConflictException("Событие должно быть в состоянии PENDING для публикации");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;

                case REJECT_EVENT:
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new ConflictException("Событие должно быть в состоянии PENDING для отклонения");
                    }
                    event.setState(EventState.CANCELED);
                    break;

                default:
                    throw new ValidationException("Unknown state action: " + dto.getStateAction());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    public EventFullDto privateGetUserEvent(Long userId, Long eventId, HttpServletRequest request) {
        log.info("userId: {}", userId);
        try {
            statClient.createStats(new EndpointHit(
                    null,
                    "event-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(FORMATTER)
            ));

            if (!userRepository.existsById(userId)) {
                throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
            }

            Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                    .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));

            return eventMapper.toEventFullDto(event);
        } catch (Exception e) {
            log.error("Error occurred while retrieving event for userId: {} and eventId: {}", userId, eventId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = checkUserExists(userId);
        Category category = checkCategoryExists(newEventDto.getCategory());

        LocalDateTime eventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER);
        if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(
                    "Дата и время события не может быть раньше, чем через два часа от текущего момента");
        }

        Location location = locationRepository.save(locationMapper.toEntity(newEventDto.getLocation()));

        Event event = eventMapper.toEvent(newEventDto, initiator, category, location);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequestDto updateRequest) {
        Event event = checkEventExists(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    "Пользователь с id=" + userId + " не является владельцем события с id=" + eventId);
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Изменять можно только события в состоянии PENDING или CANCELLED");
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDateTime = LocalDateTime.parse(updateRequest.getEventDate(), FORMATTER);
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "Дата и время события не может быть раньше, чем через два часа от текущего момента");
            }
        }

        Category category = updateRequest.getCategory() != null ? checkCategoryExists(updateRequest.getCategory()) : null;
        Location location = updateRequest.getLocation() != null
                ? locationRepository.save(locationMapper.toEntity(updateRequest.getLocation()))
                : null;

        eventMapper.updateEventFromDto(updateRequest, category, location, event);

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UpdateUserStateAction.SEND_TO_REVIEW) {
                if (event.getState() == EventState.CANCELED) {
                    event.setState(EventState.PENDING);
                } else {
                    throw new ConflictException("Событие уже находится в состоянии ожидания модерации");
                }
            } else if (updateRequest.getStateAction() == UpdateUserStateAction.CANCEL_REVIEW) {
                if (event.getState() == EventState.PENDING) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new ConflictException("Событие уже находится в состоянии отмены");
                }
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getSubscribedEvents(Long userId, int from, int size, HttpServletRequest request) {
        statClient.createStats(new EndpointHit(
                null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(FORMATTER)
        ));

        Pageable pageable = PageRequest.of(
                from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));

        Specification<Event> spec = (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Subscription> subRoot = subquery.from(Subscription.class);
            subquery.select(subRoot.get("subscribedTo").get("id"))
                    .where(cb.equal(subRoot.get("subscriber").get("id"), userId));
            return cb.and(
                    root.get("initiator").get("id").in(subquery),
                    cb.equal(root.get("state"), EventState.PUBLISHED)
            );
        };

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    private Event checkEventExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));
    }

    private Category checkCategoryExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID=" + catId + " не найдена"));
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден"));
    }
}
