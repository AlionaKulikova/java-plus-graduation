package ewm.eventandadditional.event.controller;


import ewm.eventandadditional.event.service.PrivateEventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.NewEventDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventUserRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrivateEventController {
    final PrivateEventService privateEventService;

    @GetMapping
    public List<EventShortDto> getAllBy(@PathVariable long userId, @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return privateEventService.getAllBy(userId, PageRequest.of(from, size));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto create(@PathVariable long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return privateEventService.create(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getBy(@PathVariable long userId, @PathVariable long eventId) {
        return privateEventService.getBy(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateBy(@PathVariable long userId, @PathVariable long eventId,
                                 @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return privateEventService.updateBy(userId, eventId, updateEventUserRequest);
    }
}
