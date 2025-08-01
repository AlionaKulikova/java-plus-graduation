package ewm.request.service.impl;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventState;
import ewm.interaction.dto.request.ParticipationRequestDto;
import ewm.interaction.dto.request.RequestStatus;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.PermissionException;
import ewm.interaction.feign.EventFeignClient;
import ewm.interaction.feign.UserFeignClient;
import ewm.request.mapper.RequestMapper;
import ewm.request.mapper.User;
import ewm.request.mapper.UserMapper;
import ewm.request.model.ParticipationRequest;
import ewm.request.repository.RequestRepository;
import ewm.request.service.interfaces.RequestServicePrivate;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.client.CollectorClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestServicePrivateImpl implements RequestServicePrivate {
    private final RequestRepository requestRepository;
    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;
    private final RequestMapper requestMapper;
    private final UserMapper userMapper;
    private final CollectorClient collectorClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId)
                .stream().map(requestMapper::toParticipantRequestDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос уже существует");
        }
        UserDto userDto;

        try {
            userDto = userFeignClient
                    .getUsers(List.of(userId), 0, 10)
                    .getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        User requester = userMapper.toUser(userDto);
        EventFullDto event;

        try {
            event = eventFeignClient.getEventByID(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Событие с ID=" + eventId + " не найдено");
        }

        if (requester.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор события не может подать запрос на участие");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие ещё не опубликовано");
        }
        long confirmedRequests = requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == confirmedRequests) {
            throw new ConflictException("Лимит участников исчерпан");
        }
        ParticipationRequest request = requestMapper.toParticipationRequest(event, requester);
        request.setCreated(LocalDateTime.now());

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        collectorClient.registrationInEvent(userId, eventId);

        return requestMapper.toParticipantRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long requestId, Long userId) {
        Optional<UserDto> optionalRequester = userFeignClient
                .getUsers(List.of(userId), 0, 10)
                .stream()
                .findFirst();

        if (optionalRequester.isEmpty()) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID=" + requestId + " не найден"));

        if (!userId.equals(participationRequest.getRequesterId())) {
            throw new PermissionException("Список запросов доступен только инициатору события");
        }

        if (participationRequest.getStatus().equals(RequestStatus.PENDING)) {
            participationRequest.setStatus(RequestStatus.CANCELED);
        }

        return requestMapper.toParticipantRequestDto(participationRequest);
    }
}