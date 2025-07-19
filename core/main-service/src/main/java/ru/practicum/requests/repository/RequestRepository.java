package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.model.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;


import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findByRequesterId(Long requesterId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<Request> findAllByIdIn(List<Long> requestIds);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

}
