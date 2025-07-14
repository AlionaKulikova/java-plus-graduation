package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequestDto;
import ru.practicum.events.service.EventService;

import java.util.List;

import static ru.practicum.util.PathConstants.ADMIN_EVENTS;
import static ru.practicum.util.PathConstants.EVENT_ID;

@RestController
@RequestMapping(ADMIN_EVENTS)
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventById(@RequestParam(required = false) List<Long> userIds,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(name = "from", required = false, defaultValue = "0") Long from,
                                           @RequestParam(name = "size", required = false, defaultValue = "10") Long size) {
        return eventService.getAdminEventById(userIds, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping(EVENT_ID)
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequestDto dto) {
        EventFullDto eventFullDto = eventService.updateEventAdmin(eventId, dto);
        return ResponseEntity.ok(eventFullDto);
    }
}
