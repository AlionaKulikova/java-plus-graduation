package ewm.subscription.controller;

import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.util.PathConstants;
import ewm.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(PathConstants.PATH_TO_USER)
@RequiredArgsConstructor
public class SubscriptionControllerPrivate {
    private final SubscriptionService subscriptionService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(PathConstants.PATH_TO_SUBSCRIPTION)
    SubscriptionDto subscribe(@PathVariable Long userId, @PathVariable Long subscribedToId) {
        return subscriptionService.subscribe(userId, subscribedToId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(PathConstants.PATH_TO_SUBSCRIPTION)
    void unsubscribe(@PathVariable Long userId, @PathVariable Long subscribedToId) {
        subscriptionService.unsubscribe(userId, subscribedToId);
    }

    @GetMapping(PathConstants.SUBSCRIPTIONS)
    Set<UserShortDto> getSubscriptions(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return subscriptionService.getSubscriptions(userId, PageRequest.of(from, size));
    }

    @GetMapping(PathConstants.SUBSCRIBERS)
    Set<UserShortDto> getSubscribers(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "10") int size) {
        return subscriptionService.getSubscribers(userId, PageRequest.of(from, size));
    }
}