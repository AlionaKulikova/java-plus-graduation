package ewm.eventservice.event.service.interfaces;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.event.event.ParamsEventPublic;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventServicePublic {
    List<EventShortDto> getEventsByParams(ParamsEventPublic params, Pageable pageRequest);

    EventFullDto getEventByID(Long eventId, Long userId);

    List<EventFullDto> getRecommendations(Long userId, int maxResults);

    void like(Long eventId, Long userId);
}