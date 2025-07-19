package ewm.event.service;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.PublicEventParam;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getAllBy(PublicEventParam publicEventParam, Pageable pageRequest);

    EventFullDto getBy(long eventId);
}
