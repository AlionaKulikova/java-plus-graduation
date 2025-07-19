package ru.practicum.request.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestsRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long userId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);


    List<ParticipationRequest> findByRequesterId(Long requesterId);

    @Query("select participationRequest from ParticipationRequest participationRequest " +
            "where participationRequest.event.id = :event " +
            "and participationRequest.id IN (:requestIds)")
    List<ParticipationRequest> findByEventIdAndRequestsIds(@Param("event") Long eventId,
                                                           @Param("requestIds") List<Long> requestIds);

    @Query("select participationRequest from ParticipationRequest participationRequest " +
            "where participationRequest.event.id = :eventId " +
            "and participationRequest.event.initiator.id = :userId")
    List<ParticipationRequest> findByEventIdAndInitiatorId(@Param("eventId") Long eventId,
                                                           @Param("userId") Long userId);

    @Query("select p from ParticipationRequest p " +
            "where p.status = 'CONFIRMED' " +
            "and p.event.id IN (:events)")
    List<ParticipationRequest> findConfirmedToListEvents(@Param("events") List<Long> events);

    @Query("select p from ParticipationRequest p where p.event.id = :eventId and p.status = 'CONFIRMED'")
    List<ParticipationRequest> findByEventIdConfirmed(@Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestsId);
}