package ewm.eventandadditional.event.service;


import ewm.interaction.dto.eventandadditional.event.AdminEventParam;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventAdminRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getAllBy(AdminEventParam eventParam, Pageable pageRequest);

    EventFullDto updateBy(long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
