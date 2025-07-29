package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.EventAndSimilarity;
import ru.practicum.ewm.stats.analyzer.model.Similarity;

import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    @Query("SELECT s FROM Similarity s " +
            "WHERE (s.eventaId = :eventId OR s.eventbId = :eventId) " +
            "AND NOT (s.eventaId IN :interactedList " +
            "AND s.eventbId IN :interactedList) " +
            "ORDER BY s.score DESC")
    Page<Similarity> findSimilaritiesExcludingInteracted(
            @Param("eventId") Long eventId,
            @Param("interactedList") List<Long> interactedList,
            Pageable pageable
    );

    @Query(value = "SELECT CASE " +
            "WHEN s.eventa_id IN :notInteractedList AND s.eventb_id IN :interactedList THEN s.eventa_id " +
            "WHEN s.eventb_id IN :notInteractedList AND s.eventa_id IN :interactedList THEN s.eventb_id " +
            "END AS event_id " +
            "FROM similarity s " +
            "WHERE (s.eventa_id IN :notInteractedList AND s.eventb_id IN :interactedList) " +
            "   OR (s.eventb_id IN :notInteractedList AND s.eventa_id IN :interactedList) " +
            "ORDER BY s.score DESC",
            nativeQuery = true)
    List<Long> findMostSimilarEventsIds(
            @Param("interactedList") List<Long> interactedList,
            @Param("notInteractedList") List<Long> notInteractedList,
            Pageable pageable
    );

    @Query("SELECT NEW ru.practicum.ewm.stats.analyzer.model.EventAndSimilarity(" +
            "CASE WHEN s.eventaId = :eventId THEN s.eventbId ELSE s.eventaId END, " +
            "s.score) " +
            "FROM Similarity s " +
            "WHERE (s.eventaId = :eventId OR s.eventbId = :eventId) " +
            "AND (s.eventaId IN :interactedList OR s.eventbId IN :interactedList) " +
            "ORDER BY s.score DESC")
    List<EventAndSimilarity> findSimilarEvents(
            @Param("eventId") Long eventId,
            @Param("interactedList") List<Long> interactedList
    );

}