package ewm.request.service.impl;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.request.EventRequestStatusUpdateRequest;
import ewm.interaction.dto.request.EventRequestStatusUpdateResult;
import ewm.interaction.dto.request.ParticipationRequestDto;
import ewm.interaction.dto.request.RequestStatus;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.EventFeignClient;
import ewm.interaction.feign.UserFeignClient;
import ewm.request.mapper.RequestMapper;
import ewm.request.model.ParticipationRequest;
import ewm.request.repository.RequestRepository;
import ewm.request.service.interfaces.RequestServicePublic;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestServicePublicImpl implements RequestServicePublic {
    final RequestRepository requestRepository;
    final EventFeignClient eventFeignClient;
    final UserFeignClient userFeignClient;
    final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toParticipantRequestDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequest(Long userId, Long eventId,
                                                             EventRequestStatusUpdateRequest updateRequest) {
        Optional<UserDto> optionalRequester = userFeignClient
                .getUsers(List.of(userId), 0, 10)
                .stream()
                .findFirst();

        if (optionalRequester.isEmpty()) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        EventFullDto event = eventFeignClient.getEventOfUser(userId, eventId);
        if (event == null) {
            throw new NotFoundException("Событие с ID=" + eventId + " не найдено");
        }

        List<Long> requestsIds = updateRequest.getRequestIds();
        long confirmedRequests = requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED);
        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventIdIs(requestsIds, eventId);
        long limitParticipants = event.getParticipantLimit() - confirmedRequests;
        if (limitParticipants == 0) {
            throw new ConflictException("Лимит участников исчерпан");
        }
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new IllegalArgumentException("Найдены не все запросы");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> {
                while (limitParticipants-- > 0 && !requests.isEmpty()) {
                    ParticipationRequest request = requests.removeFirst();

                    if (request.getStatus().equals(RequestStatus.PENDING)) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        requestRepository.save(request);
                        confirmed.add(request);
                    }
                }
            }
            case REJECTED ->
                    requests.forEach(participationRequest -> participationRequest.setStatus(RequestStatus.REJECTED));
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed.stream().map(requestMapper::toParticipantRequestDto).toList());
        result.setRejectedRequests(requests.stream().map(requestMapper::toParticipantRequestDto).toList());

        return result;
    }
}