package ewm.eventservice.event.controller;

import ewm.eventservice.event.service.interfaces.EventServicePrivate;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.event.event.EventShortDto;
import ewm.interaction.dto.event.event.NewEventDto;
import ewm.interaction.dto.event.event.UpdateEventUserRequest;
import ewm.interaction.util.PathConstants;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(PathConstants.PRIVATE_EVENTS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventControllerPrivate {
    final EventServicePrivate eventServicePrivate;

    @GetMapping(PathConstants.EVENT_ID)
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long userId,
                                                 @PathVariable Long eventId) {
        EventFullDto dto = eventServicePrivate.getEvent(userId, eventId);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(@PathVariable Long userId,
                                                 @Valid @RequestBody NewEventDto newEventDto) {
        EventFullDto event = eventServicePrivate.addEvent(userId, newEventDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        List<EventShortDto> events = eventServicePrivate.getEvents(userId, PageRequest.of(from, size));

        return ResponseEntity.ok(events);
    }

    @PatchMapping(PathConstants.EVENT_ID)
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        EventFullDto updateEvent = eventServicePrivate.updateEvent(userId, eventId, updateRequest);

        return ResponseEntity.ok(updateEvent);
    }
}