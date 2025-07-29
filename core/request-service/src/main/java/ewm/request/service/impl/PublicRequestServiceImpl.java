package ewm.request.service.impl;

import ewm.client.grpcclient.CollectorClient;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventState;
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
import ewm.request.service.PublicRequestService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.grpc.stats.action.UserActionMessages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicRequestServiceImpl implements PublicRequestService {
    final RequestRepository requestRepository;
    final UserFeignClient userFeignClient;
    final EventFeignClient eventFeignClient;
    final RequestMapper requestMapper;
    final UserMapper userMapper;
    final CollectorClient collectorClient;

    @Override
    public List<ParticipationRequestDto> getSentBy(long userId) {
        return requestRepository.findAllByRequesterId(userId)
                .stream().map(requestMapper::toParticipantRequestDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto send(Long userId, Long eventId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Повторные запросы на участие запрещены");
        }

        UserDto userDto;
        try {
            userDto = userFeignClient
                    .findAllBy(List.of(userId), 0, 10)
                    .getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        User requester = userMapper.toUser(userDto);

        EventFullDto event;
        try {
            event = eventFeignClient.getBy(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Событие с Id = " + eventId + " не найдено");
        }

        if (requester.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Нельзя делать запрос на свое событие");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Заявка должна быть в состоянии PUBLISHED");
        }

        long confirmedRequests = requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == confirmedRequests) {
            throw new ConflictException("Лимит запросов исчерпан");
        }
        ParticipationRequest request = requestMapper.toParticipationRequest(event, requester);

        request.setCreated(LocalDateTime.now());

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        collectorClient.sendUserAction(userId, eventId, UserActionMessages.ActionTypeProto.ACTION_REGISTER);
        return requestMapper.toParticipantRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancel(long requestId, long userId) {
        Optional<UserDto> optionalRequester = userFeignClient
                .findAllBy(List.of(userId), 0, 10)
                .stream()
                .findFirst();

        if (optionalRequester.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        if (userId != participationRequest.getRequesterId()) {
            throw new PermissionException("Доступ запрещен. Отменять может только владелец");
        }

        if (participationRequest.getStatus().equals(RequestStatus.PENDING)) {
            participationRequest.setStatus(RequestStatus.CANCELED);
        }

        return requestMapper.toParticipantRequestDto(participationRequest);
    }
}
