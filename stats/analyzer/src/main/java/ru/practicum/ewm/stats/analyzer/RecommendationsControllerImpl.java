package ru.practicum.ewm.stats.analyzer;

import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.feign.EventFeignClient;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.stats.analyzer.model.EventAndSimilarity;
import ru.practicum.ewm.stats.analyzer.model.Similarity;
import ru.practicum.ewm.stats.analyzer.repository.ActionRepository;
import ru.practicum.ewm.stats.analyzer.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.grpc.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.RecommendationsMessages;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsControllerImpl extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final ActionRepository actionRepository;
    private final SimilarityRepository similarityRepository;
    private final EventFeignClient eventFeignClient;
    private static final Double VIEW_WEIGHT = 0.4;
    private static final Double REGISTER_WEIGHT = 0.8;
    private static final Double LIKE_WEIGHT = 1.0;
    private static final long NEIGHBORS_COUNT = 10;

    @Override
    public void getRecommendationsForUser(RecommendationsMessages.UserPredictionsRequestProto request,
                                          StreamObserver<RecommendationsMessages
                                                  .RecommendedEventProto> responseObserver) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        log.info("Выявляю рекоммендации для пользователя с id = {}", userId);
        Pageable pageable = PageRequest.of(0, maxResults);


        List<Long> interactedEventIds = actionRepository
                .findActionsByUserIdOrderByTimestamp(userId, pageable)
                .getContent();

        if (!interactedEventIds.isEmpty()) {

            List<Long> notInteractedEventIds = actionRepository.findNotInteractedEventIdsByUserId(userId);
            List<Long> mostSimilarEventsIds = similarityRepository.findMostSimilarEventsIds(
                    interactedEventIds,
                    notInteractedEventIds,
                    pageable);

            for (int i = 0; i < mostSimilarEventsIds.size()
                    && i < NEIGHBORS_COUNT; i++) {
                Map<Long, Double> eventsAndSimilarities = similarityRepository
                        .findSimilarEvents(mostSimilarEventsIds.get(i), interactedEventIds.stream().toList())
                        .stream()
                        .collect(Collectors.toMap(
                                EventAndSimilarity::getEventId,
                                EventAndSimilarity::getSimilarityScore
                        ));

                Map<Long, Double> eventsAndRatings = eventFeignClient
                        .getBy(eventsAndSimilarities.keySet())
                        .stream()
                        .collect(Collectors.toMap(
                                EventFullDto::getId,
                                EventFullDto::getRating
                        ));

                double weightedMarksSum = 0.0;
                for (Long eventId : eventsAndSimilarities.keySet()) {
                    weightedMarksSum += eventsAndSimilarities.get(eventId) * eventsAndRatings.get(eventId);
                }

                double sumSimilarities = eventsAndSimilarities
                        .values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

                double result = weightedMarksSum / sumSimilarities;

                RecommendationsMessages.RecommendedEventProto event = RecommendationsMessages.RecommendedEventProto.newBuilder()
                        .setEventId(mostSimilarEventsIds.get(i))
                        .setScore(result)
                        .build();
                responseObserver.onNext(event);

            }
        }
        responseObserver.onCompleted();

    }

    @Override
    public void getSimilarEvents(RecommendationsMessages.SimilarEventsRequestProto request,
                                 StreamObserver<RecommendationsMessages
                                         .RecommendedEventProto> responseObserver) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        List<Long> interactedEventIds = actionRepository.findEventIdsByUserId(userId);
        Pageable pageable = PageRequest.of(0, maxResults);
        List<Similarity> similarities = similarityRepository.findSimilaritiesExcludingInteracted(
                eventId,
                interactedEventIds,
                pageable).stream().toList();


        long similarEventId;
        for (Similarity similarity : similarities) {
            similarEventId = similarity.getEventaId() == eventId ? similarity.getEventaId() : similarity.getEventbId();
            RecommendationsMessages.RecommendedEventProto event = RecommendationsMessages.RecommendedEventProto.newBuilder()
                    .setEventId(similarEventId)
                    .setScore(similarity.getScore())
                    .build();
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(RecommendationsMessages.InteractionsCountRequestProto request,
                                     StreamObserver<RecommendationsMessages
                                             .RecommendedEventProto> responseObserver) {
        request.getEventIdList().forEach(eventId -> {
            double likeCount = actionRepository.countUserIdsWithSpecificActionOnly(
                    eventId,
                    ActionTypeAvro.LIKE.toString(),
                    List.of(ActionTypeAvro.VIEW.toString(), ActionTypeAvro.REGISTER.toString()));

            double registerCount = actionRepository.countUserIdsWithSpecificActionOnly(
                    eventId,
                    ActionTypeAvro.REGISTER.toString(),
                    List.of(ActionTypeAvro.VIEW.toString(), ActionTypeAvro.LIKE.toString()));

            double viewCount = actionRepository.countUserIdsWithSpecificActionOnly(
                    eventId,
                    ActionTypeAvro.VIEW.toString(),
                    List.of(ActionTypeAvro.LIKE.toString(), ActionTypeAvro.REGISTER.toString()));

            double score = likeCount * LIKE_WEIGHT
                    + registerCount * REGISTER_WEIGHT
                    + viewCount * VIEW_WEIGHT;
            RecommendationsMessages.RecommendedEventProto event = RecommendationsMessages
                    .RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(score)
                    .build();
            responseObserver.onNext(event);
        });
        responseObserver.onCompleted();
    }
}