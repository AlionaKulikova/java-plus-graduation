package ewm.requests.repository;

import ewm.requests.model.Request;
import ewm.requests.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
