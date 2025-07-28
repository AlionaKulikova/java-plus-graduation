package ewm.request.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.interaction.dto.request.RequestStatus;
import ewm.request.model.QParticipationRequest;
import ewm.request.repository.RequestRepository;
import ewm.request.service.interfaces.RequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestServiceImpl implements RequestService {

    final JPAQueryFactory jpaQueryFactory;
    final RequestRepository requestRepository;

    @Override
    public Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        QParticipationRequest qRequest = QParticipationRequest.participationRequest;
        return jpaQueryFactory
                .select(qRequest.eventId.as("eventId"), qRequest.count().as("confirmedRequests"))
                .from(qRequest)
                .where(qRequest.eventId.in(eventIds).and(qRequest.status.eq(RequestStatus.CONFIRMED)))
                .groupBy(qRequest.eventId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> Optional.ofNullable(tuple.get(1, Long.class)).orElse(0L))
                );
    }

    @Override
    public Long countAllByEventIdAndStatus(Long eventId, String requestStatus) {
        return requestRepository.countAllByEventIdAndStatusIs(eventId, RequestStatus.valueOf(requestStatus));
    }
}