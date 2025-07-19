package ewm.subscriptions.service;

import ewm.subscriptions.dto.SubscriptionDto;
import ewm.user.dto.UserShortDto;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface SubscriptionService {
    Set<UserShortDto> findFollowing(long userId, Pageable page);

    Set<UserShortDto> findFollowers(long userId, Pageable page);

    SubscriptionDto follow(long userId, long followingId);

    void unfollow(long userId, long followingId);
}
