package ewm.subscription.repository;

import ewm.subscription.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    int deleteBySubscribedId(Long subscribedId);

    Page<Subscription> findBySubscribedId(Long subscribedId, Pageable pageable);

    Page<Subscription> findBySubscriberId(Long subscriberId, Pageable pageable);

}