package ewm.request.controller;

import ewm.interaction.feign.RequestFeignClient;
import ewm.interaction.util.PathConstants;
import ewm.request.service.interfaces.RequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(PathConstants.REQUESTS)
public class RequestController implements RequestFeignClient {
    private final RequestService requestService;

    @GetMapping(PathConstants.REQUEST_CONFIRMED)
    public Map<Long, Long> getConfirmedRequests(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequests(eventIds);
    }

    @GetMapping(PathConstants.COUNT_EVENT_STATUS)
    public Long countAllByEventIdAndStatus(@PathVariable Long eventId,
                                           @PathVariable String requestStatus) {
        return requestService.countAllByEventIdAndStatus(eventId, requestStatus);
    }

    @GetMapping("/exist/{eventId}/{userId}")
    public boolean isRequestExist(@PathVariable Long eventId,
                                  @PathVariable Long userId) {
        return requestService.isRequestExist(userId, eventId);
    }

}