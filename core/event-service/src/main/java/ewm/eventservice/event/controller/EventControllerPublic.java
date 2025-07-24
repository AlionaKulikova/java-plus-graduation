package ewm.eventservice.event.controller;

import ewm.client.StatRestClient;
import ewm.dto.EndpointHitDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(PathConstants.EVENTS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventControllerPublic {
   private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   private final EventServicePublic eventServicePublic;
   private final StatRestClient statRestClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<EventShortDto> getEventsByParams(@Valid @ModelAttribute ParamsEventPublic params,
                                 @RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size,
                                 HttpServletRequest request) {
        List<EventShortDto> eventsShortDto = eventServicePublic.getEventsByParams(params, PageRequest.of(from, size));
        addHit("/events", request.getRemoteAddr());

        return eventsShortDto;
    }

    @GetMapping(PathConstants.EVENT_ID)
    ResponseEntity<EventFullDto> getEventByID(@PathVariable Long eventId, HttpServletRequest request) {
        EventFullDto eventFullDto = eventServicePublic.getEventByID(eventId);
        addHit("/events/" + eventId, request.getRemoteAddr());

        return ResponseEntity.ok(eventFullDto);
    }

    void addHit(String uri, String ip) {
        LocalDateTime now = LocalDateTime.now();
        EndpointHitDto hitDto = new EndpointHitDto("main-server", uri, ip, now.format(dateTimeFormatter));
        statRestClient.addHit(hitDto);
    }
}