package ru.practicum.requests.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.RequestUpdateDto;
import ru.practicum.requests.dto.RequestUpdateResultDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

import static ru.practicum.util.PathConstants.PRIVATE_EVENT_REQUESTS;
import static ru.practicum.util.PathConstants.PRIVATE_REQUESTS;
import static ru.practicum.util.PathConstants.PRIVATE_REQUEST_CANCEL;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class RequestControllerPrivate {

    private final RequestService requestService;

    @GetMapping(PRIVATE_REQUESTS)
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserRequests(
            @PathVariable @Positive Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping(PRIVATE_REQUESTS)
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam("eventId") @Positive Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping(PRIVATE_REQUEST_CANCEL)
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping(PRIVATE_EVENT_REQUESTS)
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping(PRIVATE_EVENT_REQUESTS)
    @ResponseStatus(HttpStatus.OK)
    public RequestUpdateResultDto updateEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody RequestUpdateDto updateDto) {
        return requestService.updateEventRequests(userId, eventId, updateDto);
    }
}