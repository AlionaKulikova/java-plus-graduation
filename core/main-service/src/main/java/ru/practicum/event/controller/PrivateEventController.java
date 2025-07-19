package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Slf4j
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventLongDto> createEvent(@PathVariable Long userId,
                                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Calling the POST request to /users/{userId}/events endpoint {}", newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, newEventDto));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllEventForUser(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {
        List<EventShortDto> listEvents = eventService.getAllEventOfUser(userId, from, size);
        log.info("Calling the GET request to /users/{userId}/events endpoint for User {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(listEvents);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventForUserById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Calling the GET request to /users/{userId}/events/{eventId} endpoint with userId {} and eventId {}",
                userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEventOfUserById(userId, eventId));
    }

    @PatchMapping(value = "/{eventId}")
    public ResponseEntity<EventLongDto> updateEventByUser(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Calling the PATCH request to /users/{userId}/events/{eventId} endpoint {}", updateEventUserRequest);
        return ResponseEntity.status(HttpStatus.OK).body(eventService.updateEventByUser(userId, eventId,
                updateEventUserRequest));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestEventByUser(@PathVariable Long userId,
                                                                               @PathVariable Long eventId) {
        log.info("Calling the GET request to /users/{userId}/events/{eventId}/requests endpoint with eventId {}", eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getRequestEventByUser(userId, eventId));
    }

    @PatchMapping(value = "/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> changeRequestEventStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Calling the PATCH request to /users/{userId}/events/{eventId}/requests endpoint with eventId {}", eventId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.changeRequestEventStatus(userId, eventId, request));
    }
}
