package ewm.request.controller;

import ewm.interaction.feign.RequestFeignClient;
import ewm.request.service.RequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/requests")
public class RequestController implements RequestFeignClient {
    final RequestService requestService;

    @GetMapping("/confirmed")
    public Map<Long, Long> getConfirmedRequestMap(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequestMap(eventIds);
    }

    @GetMapping("/count/{eventId}/{requestStatus}")
    public Long countAllByEventIdAndStatusIs(@PathVariable Long eventId,
                                             @PathVariable String requestStatus) {
        return requestService.countAllByEventIdAndStatusIs(eventId, requestStatus);
    }

    @GetMapping("/exist/{eventId}/{userId}")
    public boolean isRequestExist(@PathVariable Long eventId,
                                  @PathVariable Long userId) {
        return requestService.isRequestExist(userId, eventId);
    }


}