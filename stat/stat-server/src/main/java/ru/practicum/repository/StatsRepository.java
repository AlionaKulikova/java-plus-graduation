package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Long> {

    @Query("SELECT s.app, s.uri, COUNT(s.id) AS hits " +
            "FROM Stat s " +
            "WHERE s.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.id) DESC")
    List<Object[]> findStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s.app, s.uri, COUNT(DISTINCT s.ip) AS hits " +
            "FROM Stat s " +
            "WHERE s.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<Object[]> findUniqueStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s.app, s.uri, COUNT(s.id) AS hits " +
            "FROM Stat s " +
            "WHERE s.timestamp BETWEEN ?1 AND ?2 AND s.uri IN ?3 " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.id) DESC, s.uri ASC")
    List<Object[]> findStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT s.app, s.uri, COUNT(DISTINCT s.ip) AS hits " +
            "FROM Stat s " +
            "WHERE s.timestamp BETWEEN ?1 AND ?2 AND s.uri IN ?3 " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<Object[]> findUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
