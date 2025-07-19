package ru.practicum.request.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.controller.PrivateRequestController;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestsService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class PrivateRequestControllerImpl implements PrivateRequestController {
    private final RequestsService requestsService;

    public List<ParticipationRequestDto> getRequest(Long userId) {
        return requestsService.getRequest(userId);
    }

    public ParticipationRequestDto setRequest(Long eventId,
                                              Long userId) {
        return requestsService.setRequest(eventId, userId);
    }

    public ParticipationRequestDto updateRequest(Long userId,
                                                 Long requestId) {
        return requestsService.updateRequest(userId, requestId);
    }
}