package ewm.subscription.repository;

import ewm.subscription.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    int deleteByFollowingId(long followingId);

    Page<Subscription> findByFollowingId(long followingId, Pageable pageable);

    Page<Subscription> findByFollowerId(long followerId, Pageable pageable);

}
