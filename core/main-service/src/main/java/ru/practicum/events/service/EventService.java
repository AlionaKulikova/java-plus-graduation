package ru.practicum.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.events.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, int from, int size,
                                        HttpServletRequest request);

    List<EventFullDto> getAdminEventById(List<Long> userIds, List<String> states, List<Long> categories, String rangeStart,
                                         String rangeEnd, Long from, Long size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto dto);

    EventFullDto privateGetUserEvent(Long userId, Long eventId, HttpServletRequest request);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequestDto updateRequest);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    List<EventShortDto> getSubscribedEvents(Long userId, int from, int size, HttpServletRequest request);

}
