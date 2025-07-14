package ru.practicum.subscriptions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.subscriptions.model.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberIdAndSubscribedToId(Long subscriberId, Long subscribedToId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.subscriber JOIN FETCH s.subscribedTo WHERE s.subscriber.id = :subscriberId")
    List<Subscription> findBySubscriberId(@Param("subscriberId") Long subscriberId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.subscriber JOIN FETCH s.subscribedTo WHERE s.subscribedTo.id = :subscribedToId")
    List<Subscription> findBySubscribedToId(@Param("subscribedToId") Long subscribedToId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.subscribedTo.id = :subscribedToId")
    int deleteBySubscriberIdAndSubscribedToId(@Param("subscriberId") Long subscriberId, @Param("subscribedToId") Long subscribedToId);
}
