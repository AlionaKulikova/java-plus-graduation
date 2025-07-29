package ru.practicum.ewm.dao;

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

@Repository
public class AggregatorRepository {
    private final UserActionWeightConfig userActionWeightConfig;

    // веса действий пользователя с мероприятиями
    // ключ — это ID мероприятия, а значение — ещё одно отображение, где ключ — ID пользователя,
    // а значение — максимальный вес из всех его действий с этим мероприятием
    private final Map<Long, Map<Long, Double>> userActionsWeight = new HashMap<>();

    // общие суммы весов каждого из мероприятий,
    // где ключ — ID мероприятия, а значение — сумма весов действий пользователей с ним
    private final Map<Long, Double> eventsWeightsSum = new HashMap<>();

    // сумма минимальных весов для каждой пары мероприятий,
    // где ключ - ID одного из мероприятий, а значением — ещё одно отображение,
    // где ключ — ID второго мероприятия, а значение — сумма их минимальных весов
    private final Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>();

    public AggregatorRepository(UserActionWeightConfig userActionWeightConfig) {
        this.userActionWeightConfig = userActionWeightConfig;
    }

    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();

        double oldWeight = userActionsWeight.computeIfAbsent(eventId, e -> new HashMap<>()).getOrDefault(userId, 0.0);
        double newWeight = getUserActionWeight(userAction.getActionType()); // определим новый вес действия пользователя с мероприятием

        if (oldWeight >= newWeight) {
            // вес не изменился, ничего делать не надо
            return List.of();
        }

        // обновим вес мероприятия в мапе, который им дал пользователь
        //userActionsWeight.get(eventId).merge(userId, newWeight, Math::max);
        userActionsWeight.computeIfAbsent(eventId, e -> new HashMap<>()).merge(userId, newWeight, Math::max);

        // обновляем частную сумму весов мероприятия
        double oldSum = eventsWeightsSum.getOrDefault(eventId, 0.0);
        double newSum = oldSum - oldWeight + newWeight;
        eventsWeightsSum.put(eventId, newSum);

        List<EventSimilarityAvro> eventSimilarityAvros = new ArrayList<>(); // список для возврата

        // обновим частные суммы минимальных весов для каждой пары мероприятий
        for (long otherEventId : userActionsWeight.keySet()) {
            if (otherEventId == eventId ||
                    !userActionsWeight.get(otherEventId).containsKey(userId)) {
                // если событие это же или пользователь не взаимодействовал с другим мероприятием, то пропускаем
                continue;
            }
            // обновим сумму минимальных весов пар мероприятий
            double newSumMinPairWeight = updateMinWeightSum(eventId, otherEventId, userId, oldWeight, newWeight);
            // вычислим новое значение коэффициента сходства мероприятий
            double similarity = calcSimilarity(eventId, otherEventId, newSumMinPairWeight);
            // добавим новое значение сходства в список
            eventSimilarityAvros.add(getEventSimilarityAvro(eventId, otherEventId, similarity, userAction.getTimestamp()));
        }
        return eventSimilarityAvros;
    }

    private EventSimilarityAvro getEventSimilarityAvro(long eventId, long otherEventId, double similarity, Instant timestamp) {
        long firstEventId = Math.min(eventId, otherEventId);
        long secondEventId = Math.max(eventId, otherEventId);

        return EventSimilarityAvro.newBuilder()
                .setEventA(firstEventId)
                .setEventB(secondEventId)
                .setTimestamp(timestamp)
                .setScore(similarity).build();
    }

    private double calcSimilarity(long eventId, long otherEventId, double newSumMinPairWeight) {
        if (newSumMinPairWeight == 0) return 0;

        double sumEventWeight = eventsWeightsSum.get(eventId);
        double sumOtherEventWeight = eventsWeightsSum.get(otherEventId);
        return newSumMinPairWeight / (Math.sqrt(sumEventWeight) * Math.sqrt(sumOtherEventWeight));
    }

    private double updateMinWeightSum(long eventId, long otherEventId, long userId, double oldWeight, double newWeight) {
        double oldWeightOtherEvent = userActionsWeight.get(otherEventId).get(userId);

        double oldMinPairWeight = Math.min(oldWeight, oldWeightOtherEvent);
        double newMinPairWeight = Math.min(newWeight, oldWeightOtherEvent);

        // так как сумма минимальных весов пар мероприятий одинакова для двух мероприятий вне зависимости от их порядка,
        // то в мапе minWeightsSum реализовано хранения мероприятий по следующему принципу:
        // ключ - ID мероприятия меньший, чем ID других мероприятий, которые указаны в значении мапы
        // общее количество пар мероприятий определяется по формуле: n * (n-1) / 2, где n - количество мероприятий
        // поэтому, получаем значение из мапы по минимальному eventId
        long firstEventId = Math.min(eventId, otherEventId);
        long secondEventId = Math.max(eventId, otherEventId);

        double oldSumMinPairWeight = minWeightsSum.computeIfAbsent(firstEventId,
                k -> new HashMap<>()).getOrDefault(secondEventId, 0.0);

        // если минимальный вес пар не изменился, то возвращаем старую сумму минимальных весов
        if (oldMinPairWeight == newMinPairWeight) return oldSumMinPairWeight;

        double newSumMinPairWeight = oldSumMinPairWeight - oldMinPairWeight + newMinPairWeight;
        minWeightsSum.computeIfAbsent(firstEventId, k -> new HashMap<>()).put(secondEventId, newSumMinPairWeight);
        return newSumMinPairWeight;
    }

    private double getUserActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            case LIKE -> userActionWeightConfig.getLIKE();
        };
    }
}
