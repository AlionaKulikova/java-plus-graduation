package ewm.eventservice.event.service.interfaces;

import ewm.interaction.dto.event.event.EventFullDto;

public interface EventService {
    EventFullDto getEventOfUser(Long eventId, Long userId);

    EventFullDto getEventByID(Long eventId);

}