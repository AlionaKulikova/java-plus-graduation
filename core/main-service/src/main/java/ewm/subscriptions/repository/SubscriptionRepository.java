package ewm.subscriptions.repository;

import ewm.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    int deleteByFollowingId(long followingId);
}
