package ewm.request.controller;

import ewm.interaction.dto.request.EventRequestStatusUpdateRequest;
import ewm.interaction.dto.request.EventRequestStatusUpdateResult;
import ewm.interaction.dto.request.ParticipationRequestDto;
import ewm.interaction.util.PathConstants;
import ewm.request.service.interfaces.RequestServicePublic;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(PathConstants.PRIVATE_EVENT_REQUESTS)
public class RequestControllerPublic {
    private final RequestServicePublic requestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateEventRequest(@PathVariable @Positive Long userId,
                                                             @PathVariable @Positive Long eventId,
                                                             @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateEventRequest(userId, eventId, updateRequest);
    }
}