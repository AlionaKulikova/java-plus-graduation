package ewm.eventservice.event.service.interfaces;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.ParamsEventAdmin;
import ewm.interaction.dto.event.event.UpdateEventAdminRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventServiceAdmin {
    List<EventFullDto> getEventsByParams(ParamsEventAdmin params, Pageable pageRequest);

    EventFullDto updateEventByID(Long eventId, UpdateEventAdminRequest dto);
}