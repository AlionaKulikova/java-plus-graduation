package ru.practicum.ewm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class AggregatorRepository {
    private final UserActionWeightConfig userActionWeightConfig;
    private final Map<Long, Map<Long, Double>> weightsUserActions = new HashMap<>();
    private final Map<Long, Double> sumWeightsEvents = new HashMap<>();
    private final Map<Long, Map<Long, Double>> sumMinWeights = new HashMap<>();

    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();

        Map<Long, Double> userWeightsForEvent = weightsUserActions.computeIfAbsent(eventId, e -> new HashMap<>());
        double oldEventWeight = userWeightsForEvent.getOrDefault(userId, 0.0);
        double newEventWeight = getWeightOfUserAction(userAction.getActionType());

        if (newEventWeight <= oldEventWeight) {
            return List.of();
        }
        userWeightsForEvent.put(userId, newEventWeight);

        double oldSumWeight = sumWeightsEvents.getOrDefault(eventId, 0.0);
        double newSumWeight = oldSumWeight - oldEventWeight + newEventWeight;
        sumWeightsEvents.put(eventId, newSumWeight);

        List<EventSimilarityAvro> similarityList = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : weightsUserActions.entrySet()) {
            long anotherEventId = entry.getKey();

            if (eventId == anotherEventId) {
                continue;
            }

            Map<Long, Double> userWeightsForOtherEvent = entry.getValue();
            if (!userWeightsForOtherEvent.containsKey(userId)) {
                continue;
            }
            double newSumMinPairWeight = updateMinWeightSum(eventId, anotherEventId, userId,
                    oldEventWeight, newEventWeight);
            double similarity = computeSimilarity(eventId, anotherEventId, newSumMinPairWeight);
            similarityList.add(createEventSimilarityAvro(eventId, anotherEventId, similarity,
                    userAction.getTimestamp()));
        }

        return similarityList;
    }

    private EventSimilarityAvro createEventSimilarityAvro(Long eventId,
                                                          Long anotherEventId,
                                                          double similarity,
                                                          Instant timestamp) {
        long firstEventId = Math.min(eventId, anotherEventId);
        long secondEventId = Math.max(eventId, anotherEventId);

        return EventSimilarityAvro.newBuilder()
                .setEventA(firstEventId)
                .setEventB(secondEventId)
                .setTimestamp(timestamp)
                .setScore(similarity)
                .build();
    }

    private double computeSimilarity(Long eventId, Long otherEventId, double sumMinPairWeight) {
        if (sumMinPairWeight == 0) {
            return 0;
        }

        double sumWeightEvent = sumWeightsEvents.getOrDefault(eventId, 0.0);
        double sumWeightOtherEvent = sumWeightsEvents.getOrDefault(otherEventId, 0.0);

        if (sumWeightEvent == 0 || sumWeightOtherEvent == 0) {
            return 0;
        }

        return sumMinPairWeight / (Math.sqrt(sumWeightEvent) * Math.sqrt(sumWeightOtherEvent));
    }

    private double updateMinWeightSum(Long eventId, Long anotherEventId, Long userId,
                                      double oldWeight, double newWeight) {
        Map<Long, Double> userWeightsForAnotherEvent = weightsUserActions.get(anotherEventId);
        if (userWeightsForAnotherEvent == null) {
            return 0;
        }

        double oldWeightAnotherEvent = userWeightsForAnotherEvent.getOrDefault(userId, 0.0);

        double oldMinPairWeight = Math.min(oldWeight, oldWeightAnotherEvent);
        double newMinPairWeight = Math.min(newWeight, oldWeightAnotherEvent);

        long firstEventId = Math.min(eventId, anotherEventId);
        long secondEventId = Math.max(eventId, anotherEventId);

        Map<Long, Double> minWeightsForFirstEvent = sumMinWeights.computeIfAbsent(firstEventId, k -> new HashMap<>());
        double oldSumMinPairWeight = minWeightsForFirstEvent.getOrDefault(secondEventId, 0.0);

        if (oldMinPairWeight == newMinPairWeight) {
            return oldSumMinPairWeight;
        }

        double newSumMinPairWeight = oldSumMinPairWeight - oldMinPairWeight + newMinPairWeight;
        minWeightsForFirstEvent.put(secondEventId, newSumMinPairWeight);

        return newSumMinPairWeight;
    }

    private double getWeightOfUserAction(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case LIKE -> userActionWeightConfig.getLIKE();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            default -> 0;
        };
    }
}