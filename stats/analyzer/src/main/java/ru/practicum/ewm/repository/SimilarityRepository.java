package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Similarity;

import java.util.List;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    @Query("""
            select new Similarity(s.key, s.score, s.timestamp)
            from Similarity s where s.key.eventId = :eventId or s.key.otherEventId = :eventId
            """)
    List<Similarity> findAllContainsEventId(long eventId);

    @Query("""
            select new Similarity(s.key, s.score, s.timestamp) from Similarity s
            where s.key.eventId in :eventId or s.key.otherEventId in :eventId
            order by s.score desc limit :maxResults
            """)
    List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> eventId, int maxResults);
}
