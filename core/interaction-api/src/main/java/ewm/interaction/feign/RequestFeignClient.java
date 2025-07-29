package ewm.interaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/requests")
public interface RequestFeignClient {
    @GetMapping("/confirmed")
    Map<Long, Long> getConfirmedRequestMap(@RequestParam List<Long> eventIds);

    @GetMapping("/count/{eventId}/{requestStatus}")
    Long countAllByEventIdAndStatusIs(@PathVariable Long eventId,
                                      @PathVariable String requestStatus);

    @GetMapping("/exist/{eventId}/{userId}")
    boolean isRequestExist(@PathVariable Long eventId,
                           @PathVariable Long userId);
}
