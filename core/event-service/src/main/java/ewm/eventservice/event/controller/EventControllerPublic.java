package ewm.eventservice.event.controller;

import ewm.eventservice.event.service.interfaces.EventServicePublic;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.event.event.ParamsEventPublic;
import ewm.interaction.util.PathConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(PathConstants.EVENTS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventControllerPublic {
    private final EventServicePublic eventServicePublic;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByParams(@Valid @ModelAttribute ParamsEventPublic params,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 HttpServletRequest request) {
        List<EventShortDto> eventsShortDto = eventServicePublic.getEventsByParams(params, PageRequest.of(from, size));

        return eventsShortDto;
    }

    @GetMapping(PathConstants.EVENT_ID)
    EventFullDto getBy(@PathVariable Long eventId, HttpServletRequest request,
                       @RequestHeader("X-EWM-USER-ID") Long userId) {
        EventFullDto event = eventServicePublic.getEventByID(eventId, userId);

        return event;
    }

    @GetMapping("/recommendations")
    List<EventFullDto> getRecommendations(HttpServletRequest request,
                                          @RequestHeader("X-EWM-USER-ID") Long userId,
                                          @RequestParam("maxResults") int maxResults) {
        return eventServicePublic.getRecommendations(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    void like(@PathVariable Long eventId, HttpServletRequest request,
              @RequestHeader("X-EWM-USER-ID") Long userId) {
        eventServicePublic.like(eventId, userId);
    }
}