package ewm.request.service.interfaces;

import ewm.interaction.dto.request.EventRequestStatusUpdateRequest;
import ewm.interaction.dto.request.EventRequestStatusUpdateResult;
import ewm.interaction.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestServicePublic {
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequest(Long userId, Long eventId,
                                                      EventRequestStatusUpdateRequest updateRequest);
}