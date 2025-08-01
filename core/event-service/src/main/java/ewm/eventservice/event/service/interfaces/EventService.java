package ewm.eventservice.event.service.interfaces;

import ewm.interaction.dto.event.event.EventFullDto;

import java.util.List;
import java.util.Set;

public interface EventService {
    EventFullDto getEventOfUser(Long eventId, Long userId);

    EventFullDto getEventByID(Long eventId);

    List<EventFullDto> getEventsByIds(Set<Long> eventIds);

}