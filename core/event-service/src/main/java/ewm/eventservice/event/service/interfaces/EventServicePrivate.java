package ewm.eventservice.event.service.interfaces;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.event.event.NewEventDto;
import ewm.interaction.dto.event.event.UpdateEventUserRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventServicePrivate {

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEvents(Long userId, Pageable pageRequest);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);
}