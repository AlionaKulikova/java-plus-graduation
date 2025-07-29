package ewm.eventandadditional.event.controller;

import ewm.eventandadditional.event.service.EventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.feign.EventFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/events/feign")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController implements EventFeignClient {
    final EventService eventService;

    @GetMapping("/{eventId}/{userId}")
    public EventFullDto getBy(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.getBy(eventId, userId);
    }

    @GetMapping("/{eventId}")
    @Override
    public EventFullDto getBy(long eventId) {
        return eventService.getBy(eventId);
    }


    @GetMapping
    @Override
    public List<EventFullDto> getBy(@RequestParam Set<Long> eventIds) {
        return eventService.getBy(eventIds);
    }
}
