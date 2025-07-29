package ewm.eventandadditional.event.service;

import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.PublicEventParam;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getAllBy(PublicEventParam publicEventParam, Pageable pageRequest);

    EventFullDto getBy(long eventId, long userId);

    List<EventFullDto> getRecommendations(long userId, int maxResults);

    void like(long eventId, long userId);
}
