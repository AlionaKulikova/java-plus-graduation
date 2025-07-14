package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequestDto;
import ru.practicum.events.service.EventService;

import java.util.List;

import static ru.practicum.util.PathConstants.EVENT_ID;
import static ru.practicum.util.PathConstants.PRIVATE_EVENTS;
import static ru.practicum.util.PathConstants.SUBSCRIPTIONS;

@RestController
@RequestMapping(PRIVATE_EVENTS)
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping(path = EVENT_ID)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable("userId") Long userId,
                                                 @PathVariable("eventId") Long eventId,
                                                 HttpServletRequest request) {
        EventFullDto dto = eventService.privateGetUserEvent(userId, eventId, request);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {
        log.info("New event: {}", newEventDto);
        EventFullDto event = eventService.addEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(0) int size) {
        List<EventShortDto> events = eventService.getEvents(userId, from, size);
        return ResponseEntity.ok(events);
    }

    @PatchMapping(EVENT_ID)
    public ResponseEntity<EventFullDto> updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequestDto updateRequest) {
        EventFullDto updatedEvent = eventService.updateEvent(userId, eventId, updateRequest);
        return ResponseEntity.ok(updatedEvent);
    }

    @GetMapping(SUBSCRIPTIONS)
    public ResponseEntity<List<EventShortDto>> getSubscribedEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(0) int size,
            HttpServletRequest request) {
        List<EventShortDto> events = eventService.getSubscribedEvents(userId, from, size, request);
        return ResponseEntity.ok(events);
    }

}
