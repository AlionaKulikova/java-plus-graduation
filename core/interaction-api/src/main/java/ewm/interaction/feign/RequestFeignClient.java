package ewm.interaction.feign;

import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.util.PathConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = PathConstants.REQUESTS)
public interface RequestFeignClient {
    @GetMapping(PathConstants.REQUEST_CONFIRMED)
    Map<Long, Long> getConfirmedRequests(@RequestParam List<Long> eventIds);

    @GetMapping(PathConstants.COUNT_EVENT_STATUS)
    Long countAllByEventIdAndStatus(@PathVariable Long eventId,
                                      @PathVariable String requestStatus);
    @GetMapping("/exist/{eventId}/{userId}")
    boolean isRequestExist(@PathVariable Long eventId,
                           @PathVariable Long userId);

}