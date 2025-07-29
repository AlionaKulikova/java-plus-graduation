package ewm.eventandadditional.event.service;

import ewm.interaction.dto.eventandadditional.event.EventFullDto;

import java.util.List;
import java.util.Set;

public interface EventService {
    EventFullDto getBy(Long eventId, Long userId);

    EventFullDto getBy(long eventId);

    List<EventFullDto> getBy(Set<Long> eventIds);

}
