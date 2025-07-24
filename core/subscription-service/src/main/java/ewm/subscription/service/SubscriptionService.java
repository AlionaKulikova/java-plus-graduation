package ewm.subscription.service;

import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.interaction.dto.user.UserShortDto;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface SubscriptionService {

    SubscriptionDto subscribe(Long subscriberId, Long subscribedToId);

    void unsubscribe(Long subscriberId, Long subscribedToId);

    Set<UserShortDto> getSubscriptions(Long userId, Pageable page);

    Set<UserShortDto> getSubscribers(Long userId, Pageable page);
}