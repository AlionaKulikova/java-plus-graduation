package ewm.eventandadditional.event.controller;

import ewm.eventandadditional.event.service.PublicEventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.EventShortDto;
import ewm.interaction.dto.eventandadditional.event.PublicEventParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicEventController {
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    final PublicEventService publicEventService;


    @GetMapping
    List<EventShortDto> getAllBy(@Valid @ModelAttribute PublicEventParam publicEventParam,
                                 @RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size,
                                 HttpServletRequest request) {
        List<EventShortDto> events = publicEventService.getAllBy(publicEventParam, PageRequest.of(from, size));
        return events;
    }

    @GetMapping("/recommendations")
    List<EventFullDto> getRecommendations(HttpServletRequest request,
                                          @RequestHeader("X-EWM-USER-ID") long userId,
                                          @RequestParam("maxResults") int maxResults) {
        return publicEventService.getRecommendations(userId, maxResults);
    }

    @GetMapping("/{eventId}")
    EventFullDto getBy(@PathVariable long eventId, HttpServletRequest request,
                       @RequestHeader("X-EWM-USER-ID") long userId) {
        EventFullDto event = publicEventService.getBy(eventId, userId);
        return event;
    }

    @PutMapping("/{eventId}/like")
    void like(@PathVariable long eventId, HttpServletRequest request,
              @RequestHeader("X-EWM-USER-ID") long userId) {
        publicEventService.like(eventId, userId);
    }
}
