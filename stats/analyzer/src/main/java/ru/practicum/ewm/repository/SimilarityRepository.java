package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.similarity.Similarity;

import java.util.List;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    @Query("""
            select s
            from Similarity s
            where s.key.eventId = :eventId or s.key.otherEventId = :eventId
            """)
    List<Similarity> findAllContainsEventId(Long eventId);

    @Query("""
            select s
            from Similarity s
            where s.key.eventId in :eventIds or s.key.otherEventId in :eventIds
            order by s.score desc
            """)
    List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> eventIds, Pageable pageable);
}
