package ewm.request.repository;

import ewm.request.model.ParticipationRequest;
import ewm.request.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(long userId);

    List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(long eventId, long userId);

    List<ParticipationRequest> findAllByIdInAndEventIdIs(List<Long> eventIds, long eventId);

    long countAllByEventIdAndStatusIs(long eventId, RequestStatus status);
}
