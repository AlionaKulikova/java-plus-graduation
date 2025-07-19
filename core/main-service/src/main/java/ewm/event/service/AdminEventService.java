package ewm.event.service;

import ewm.event.dto.AdminEventParam;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.UpdateEventAdminRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getAllBy(AdminEventParam eventParam, Pageable pageRequest);

    EventFullDto updateBy(long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
