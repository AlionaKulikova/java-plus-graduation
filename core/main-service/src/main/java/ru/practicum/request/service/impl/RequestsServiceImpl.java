package ru.practicum.request.service.impl;


import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;
import ru.practicum.request.model.mapper.RequestsMapper;
import ru.practicum.request.repository.RequestsRepository;
import ru.practicum.request.service.RequestsService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestsServiceImpl implements RequestsService {

    private final RequestsRepository requestsRepository;
    private final EventService eventService;
    private final UserService userService;

    @Override
    public List<ParticipationRequestDto> getRequest(Long userId) {
        return requestsRepository.findByRequesterId(userId).stream()
                .map(RequestsMapper.REQUESTS_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto setRequest(Long eventId, Long userId) {

        Event event = eventService.getEventById(eventId);
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("You can't send request to your own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }
        int confirmedRequests = requestsRepository.findByEventIdConfirmed(eventId).size();
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException("Participant limit reached");
        }
        User user = userService.findUserById(userId);
        RequestStatus status = RequestStatus.PENDING;
        if (Boolean.TRUE.equals(!event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .status(status)
                .event(event)
                .requester(user)
                .build();

        Optional<ParticipationRequest> check = requestsRepository.findByEventIdAndRequesterId(eventId, userId);
        if (check.isPresent()) throw new ConflictException("You already have request to event");

        participationRequest = requestsRepository.save(participationRequest);
        return RequestsMapper.REQUESTS_MAPPER.toParticipationRequestDto(participationRequest);
    }

    @Override
    public ParticipationRequestDto updateRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestsRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() ->
                new NotFoundException("Request not found"));
        request.setStatus(RequestStatus.CANCELED);
        return RequestsMapper.REQUESTS_MAPPER.toParticipationRequestDto(requestsRepository.save(request));
    }


    @Override
    public List<ParticipationRequest> getAllByEventId(Long eventId) {
        return requestsRepository.findAllByEventId(eventId);
    }

    public List<ParticipationRequest> getAllByRequestIdIn(List<Long> requestsId) {
        return requestsRepository.findAllByIdIn(requestsId);
    }

    public Iterable<ParticipationRequest> findAll(BooleanExpression conditions) {
        return requestsRepository.findAll((Pageable) conditions);
    }

    public List<ParticipationRequest> saveAll(List<ParticipationRequest> requests) {
        return requestsRepository.saveAll(requests);
    }


}