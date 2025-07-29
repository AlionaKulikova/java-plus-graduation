package ewm.request.service.impl;

import ewm.interaction.dto.eventandadditional.event.EventFullDto;
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
import ewm.request.service.PrivateRequestService;
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
public class PrivateRequestServiceImpl implements PrivateRequestService {
    final RequestRepository requestRepository;
    final EventFeignClient eventFeignClient;
    final UserFeignClient userFeignClient;
    final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getReceivedBy(long userId, long eventId) {
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toParticipantRequestDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult update(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Optional<UserDto> optionalRequester = userFeignClient
                .findAllBy(List.of(userId), 0, 10)
                .stream()
                .findFirst();

        if (optionalRequester.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        EventFullDto event = eventFeignClient.getBy(userId, eventId);
        if (event == null) {
            throw new NotFoundException("Событие с Id = " + eventId + " не найден");
        }

        List<Long> requestsIds = updateRequest.getRequestIds();
        long confirmedRequests = requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED);
        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventIdIs(requestsIds, eventId);
        long limit = event.getParticipantLimit() - confirmedRequests;
        if (limit == 0) {
            throw new ConflictException("Количество подтвержденных запросов исчерпано: " + confirmedRequests);
        }
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new IllegalArgumentException("Не все запросы были найдены. Ошибка при вводе Ids");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> {
                while (limit-- > 0 && !requests.isEmpty()) {
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
