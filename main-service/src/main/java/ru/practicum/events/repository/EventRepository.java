package ru.practicum.events.repository;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.events.model.Event;

import java.util.List;
import java.util.Optional;


public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @EntityGraph(attributePaths = {"initiator", "category", "location"})
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findEventsByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = {"initiator", "category", "location"})
    Page<Event> findAll(@NonNull Specification<Event> spec, @NonNull Pageable pageable);

    List<Event> findByCategoryId(Long catId);
}