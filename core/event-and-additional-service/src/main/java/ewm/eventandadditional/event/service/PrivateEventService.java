package ewm.eventandadditional.event.service;


import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.NewEventDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventUserRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrivateEventService {
    List<EventShortDto> getAllBy(long userId, Pageable pageRequest);

    EventFullDto create(long userId, NewEventDto newEventDto);

    EventFullDto getBy(long userId, long eventId);

    EventFullDto updateBy(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);
}
