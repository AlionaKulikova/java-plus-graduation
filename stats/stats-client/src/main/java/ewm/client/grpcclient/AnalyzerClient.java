package ewm.client.grpcclient;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.RecommendationsMessages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
@Slf4j
public class AnalyzerClient {
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub blockingStub;

    public AnalyzerClient(@GrpcClient("analyzer") RecommendationsControllerGrpc
            .RecommendationsControllerBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public Set<Long> getSimilarEvents(long eventId, long userId, int maxResults) {
        RecommendationsMessages.SimilarEventsRequestProto similarRequest = RecommendationsMessages
                .SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Set<Long> eventIdsToReturn = new HashSet<>();
        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getSimilarEvents(similarRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                eventIdsToReturn.add(event.getEventId());
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
            }
        } catch (StatusRuntimeException e) {
            log.info("Неудачный вызов gRPC: {}", e.getStatus());
        }
        return eventIdsToReturn;
    }

    public Set<Long> getRecommendationsForUser(long userId, int maxResults) {
        RecommendationsMessages.UserPredictionsRequestProto userRequest = RecommendationsMessages
                .UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Set<Long> eventIdsToReturn = new HashSet<>();

        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getRecommendationsForUser(userRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
                eventIdsToReturn.add(event.getEventId());
            }
        } catch (StatusRuntimeException e) {
            log.error("Неудачный вызов gRPC" + e.getStatus());
        }
        return eventIdsToReturn;
    }

    public Set<Long> getInteractionsCount(Iterable<Long> eventIds) {
        RecommendationsMessages.InteractionsCountRequestProto interactionsRequest = RecommendationsMessages
                .InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        Set<Long> eventIdsToReturn = new HashSet<>();

        try {
            Iterator<RecommendationsMessages.RecommendedEventProto> responseIterator = blockingStub
                    .getInteractionsCount(interactionsRequest);

            while (responseIterator.hasNext()) {
                RecommendationsMessages.RecommendedEventProto event = responseIterator.next();
                log.info("Event ID: {}, Score: {}", event.getEventId(), event.getScore());
                eventIdsToReturn.add(event.getEventId());
            }
        } catch (StatusRuntimeException e) {
            log.info("Неудачный вызов gRPC: {}", e.getStatus());
        }
        return eventIdsToReturn;
    }
}
