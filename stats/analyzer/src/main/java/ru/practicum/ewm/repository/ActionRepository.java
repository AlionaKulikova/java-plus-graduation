package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Action;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ActionRepository extends JpaRepository<Action, Long> {
    @Query("select distinct a.eventId from Action a where a.userId = :userId and a.eventId in :otherEventId")
    List<Long> findEventIdsByUserIdAndEventIdIn(long userId, Set<Long> otherEventId);

    Optional<Action> findByUserIdAndEventId(long userId, long eventId);

    @Query("select distinct a.eventId from Action a where a.userId = :userId order by a.timestamp desc limit :maxResult")
    List<Long> findByUserIdOrderByTimestampDesc(long userId, int maxResult);

    List<Action> findAllByEventIdIn(Set<Long> eventIds);
}
