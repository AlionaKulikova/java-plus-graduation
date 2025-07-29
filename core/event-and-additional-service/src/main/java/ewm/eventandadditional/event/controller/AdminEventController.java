package ewm.eventandadditional.event.controller;

import ewm.eventandadditional.event.service.AdminEventService;
import ewm.interaction.dto.eventandadditional.event.AdminEventParam;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.dto.eventandadditional.event.UpdateEventAdminRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/admin/events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEventController {
    final AdminEventService adminEventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getAllBy(@Validated @ModelAttribute AdminEventParam adminEventParam,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return adminEventService.getAllBy(adminEventParam, PageRequest.of(from, size));
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateBy(@PathVariable("eventId") long eventId,
                                 @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return adminEventService.updateBy(eventId, updateEventAdminRequest);

    }
}
