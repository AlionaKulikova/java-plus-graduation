package ewm.interaction.feign;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.util.PathConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", path = PathConstants.EVENTS_FEIGN)
public interface EventFeignClient {
    @GetMapping(PathConstants.USER_ID_EVENT_ID)
    EventFullDto getEventOfUser(@PathVariable Long userId, @PathVariable Long eventId);

    @GetMapping(PathConstants.EVENT_ID)
    EventFullDto getEventByID(@PathVariable Long eventId);
}