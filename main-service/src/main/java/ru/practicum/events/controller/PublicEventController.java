package ru.practicum.events.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.PathConstants.EVENTS;
import static ru.practicum.util.PathConstants.EVENT_ID;

@RestController
@RequestMapping(EVENTS)
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false)
                                         @Size(min = 1, max = 7000)
                                         String text,
                                         @RequestParam(required = false)
                                         List<Long> categories,
                                         @RequestParam(required = false)
                                         Boolean paid,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeStart,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false")
                                         Boolean onlyAvailable,
                                         @RequestParam(required = false)
                                         String sort,
                                         @RequestParam(defaultValue = "0")
                                         @Min(value = 0)
                                         int from,
                                         @RequestParam(defaultValue = "10")
                                         int size,
                                         HttpServletRequest request) {
        return eventService.getPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping(EVENT_ID)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long eventId, HttpServletRequest request) {
        EventFullDto eventFullDto = eventService.getEventById(eventId, request);

        return ResponseEntity.ok(eventFullDto);
    }
}
