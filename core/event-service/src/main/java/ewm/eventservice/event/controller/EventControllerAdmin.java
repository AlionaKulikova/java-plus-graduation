package ewm.eventservice.event.controller;

import ewm.eventservice.event.service.interfaces.EventServiceAdmin;
import ewm.interaction.dto.event.event.ParamsEventAdmin;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.UpdateEventAdminRequest;
import ewm.interaction.util.PathConstants;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(PathConstants.ADMIN_EVENTS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventControllerAdmin {
    private final EventServiceAdmin eventServiceAdmin;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsByParams(@Validated @ModelAttribute ParamsEventAdmin params,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return eventServiceAdmin.getEventsByParams(params, PageRequest.of(from, size));
    }

    @PatchMapping(PathConstants.EVENT_ID)
    public ResponseEntity<EventFullDto> updateEventByID(@PathVariable("eventId") Long eventId,
                                 @Valid @RequestBody UpdateEventAdminRequest dto) {
        EventFullDto eventFullDto = eventServiceAdmin.updateEventByID(eventId, dto);

        return ResponseEntity.ok(eventFullDto);
    }
}