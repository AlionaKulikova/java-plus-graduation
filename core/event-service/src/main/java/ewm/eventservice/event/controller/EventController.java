package ewm.eventservice.event.controller;

import ewm.eventservice.event.service.interfaces.EventService;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.feign.EventFeignClient;
import ewm.interaction.util.PathConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(PathConstants.EVENTS_FEIGN)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController implements EventFeignClient {
    private final EventService eventService;

    @GetMapping(PathConstants.USER_ID_EVENT_ID)
    public EventFullDto getEventOfUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventOfUser(eventId, userId);
    }

    @GetMapping(PathConstants.EVENT_ID)
    @Override
    public EventFullDto getEventByID(Long eventId) {

        return eventService.getEventByID(eventId);
    }

    @GetMapping
    @Override
    public List<EventFullDto> getEventsByIds(@RequestParam Set<Long> eventIds) {
        return eventService.getEventsByIds(eventIds);
    }
}