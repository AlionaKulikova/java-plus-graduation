package ru.practicum.request.service;


import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestsService {
    List<ParticipationRequestDto> getRequest(Long userId);

    ParticipationRequestDto setRequest(Long eventId, Long userId);

    ParticipationRequestDto updateRequest(Long userId, Long requestId);

    List<ParticipationRequest> getAllByEventId(Long eventId);

    List<ParticipationRequest> getAllByRequestIdIn(List<Long> requestIds);

    List<ParticipationRequest> saveAll(List<ParticipationRequest> requests);

}