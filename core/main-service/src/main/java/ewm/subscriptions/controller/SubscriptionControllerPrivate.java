package ewm.subscriptions.controller;

import ewm.subscriptions.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ewm.subscriptions.dto.SubscriptionDto;

import java.util.List;

import static ewm.util.PathConstants.PATH_TO_SUBSCRIPTION;
import static ewm.util.PathConstants.PATH_TO_USER;
import static ewm.util.PathConstants.SUBSCRIBERS;
import static ewm.util.PathConstants.SUBSCRIPTIONS;

@RestController
@RequestMapping(PATH_TO_USER)
@RequiredArgsConstructor
public class SubscriptionControllerPrivate {
    private final SubscriptionService subscriptionService;

    @PostMapping(PATH_TO_SUBSCRIPTION)
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto subscribe(@PathVariable Long userId, @PathVariable Long subscribedToId) {
        return subscriptionService.subscribe(userId, subscribedToId);
    }

    @DeleteMapping(PATH_TO_SUBSCRIPTION)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long userId, @PathVariable Long subscribedToId) {
        subscriptionService.unsubscribe(userId, subscribedToId);
    }

    @GetMapping(SUBSCRIPTIONS)
    public List<SubscriptionDto> getSubscriptions(@PathVariable Long userId) {
        return subscriptionService.getSubscriptions(userId);
    }

    @GetMapping(SUBSCRIBERS)
    public List<SubscriptionDto> getSubscribers(@PathVariable Long userId) {
        return subscriptionService.getSubscribers(userId);
    }
}
