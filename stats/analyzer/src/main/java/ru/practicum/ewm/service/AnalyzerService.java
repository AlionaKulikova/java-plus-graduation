package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.action.Action;
import ru.practicum.ewm.model.action.ActionType;
import ru.practicum.ewm.model.similarity.Similarity;
import ru.practicum.ewm.service.action.ActionService;
import ru.practicum.ewm.service.similarity.SimilarityService;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyzerService {
    private final ActionService actionService;
    private final SimilarityService similarityService;
    private final UserActionWeightConfig userActionWeightConfig;

    public List<RecommendedEventProto> getRecommendations(UserPredictionsRequestProto request) {
        Set<Long> userEventIds = new HashSet<>(actionService.findSortedEventIdsOfUser(request.getUserId(),
                request.getMaxResults()));

        List<Similarity> similarities = similarityService.findNPairContainsEventIdsSortedDescScore(userEventIds,
                request.getMaxResults());

        Map<Long, Double> recommendedEvents = similarities.stream()
                .collect(Collectors.toMap(
                        s -> userEventIds.contains(s.getKey().getEventId())
                                ? s.getKey().getOtherEventId() : s.getKey().getEventId(),
                        Similarity::getScore,
                        Double::max));

        return recommendedEvents.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Similarity> similarities = similarityService.findAllContainsEventId(request.getEventId());

        Set<Long> allEventIds = similarities.stream()
                .flatMap(s -> Stream.of(s.getKey().getEventId(), s.getKey().getOtherEventId()))
                .collect(Collectors.toSet());

        Set<Long> userEventIds = actionService.findEventIds(request.getUserId(), allEventIds);

        similarities.removeIf(s -> userEventIds.contains(s.getKey().getEventId())
                && userEventIds.contains(s.getKey().getOtherEventId()));

        return similarities.stream()
                .sorted(Comparator.comparing(Similarity::getScore).reversed())
                .limit(request.getMaxResults())
                .map(s -> {
                    long eventId = s.getKey().getEventId() == request.getEventId()
                            ? s.getKey().getOtherEventId() : s.getKey().getEventId();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(s.getScore())
                            .build();
                })
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        Map<Long, Double> scores = actionService.findActionsByEventIds(eventIds).stream()
                .collect(Collectors.groupingBy(Action::getEventId,
                        Collectors.summingDouble(a -> getUserActionWeight(a.getActionType()))));

        return scores.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }

    private double getUserActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            case LIKE -> userActionWeightConfig.getLIKE();
        };
    }
}