package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.Action;
import ru.practicum.ewm.model.ActionType;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyzerService {
    private final ActionService actionService;
    private final SimilarityService similarityService;
    private final UserActionWeightConfig userActionWeightConfig;

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        // выгрузим из базы данных все коэффициенты подобия для пары мероприятий, в которой одно (любое) соответствует указанному
        List<Similarity> similarPair = similarityService.findAllContainsEventId(request.getEventId());

        // получим уникальные ID мероприятий, которые есть в этих парах
        Set<Long> ids = similarPair.stream().map(o -> o.getKey().getEventId()).collect(Collectors.toSet());
        Set<Long> otherIds = similarPair.stream().map(o -> o.getKey().getOtherEventId()).collect(Collectors.toSet());
        ids.addAll(otherIds);

        // найдем ID мероприятий, с которыми взаимодействовал пользователь из переданного списка
        Set<Long> userEventIds = actionService.findAllByUserIdAndEventIdIn(request.getUserId(), ids);

        // уберем из списка те коэффициенты подобия (пары мероприятий), в которых пользователь взаимодействовал с обоими мероприятиями
        similarPair.removeIf(o -> userEventIds.contains(o.getKey().getEventId()) && userEventIds.contains(o.getKey().getOtherEventId()));

        // отсортируем оставшиеся мероприятия от максимального к минимальному значению коэффициента и выберем указанное количество
        // и так же учитываем, что на выходе должны быть мероприятия, где пользователь не участвовал в них
        return similarPair.stream()
                .sorted(Comparator.comparing(Similarity::getScore, Comparator.reverseOrder()))
                .limit(request.getMaxResults())
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(o.getKey().getEventId() == request.getEventId() ? o.getKey().getOtherEventId() : o.getKey().getEventId())
                        .setScore(o.getScore())
                        .build()).toList();
    }

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        // выгрузим ID мероприятий, с которыми пользователь уже взаимодействовал
        Set<Long> actionIds = actionService.findByUserIdOrderByTimestampDesc(request.getUserId(), request.getMaxResults());

        // найдем N мероприятий, похожих на те, что отобрали, но при этом пользователь с ними не взаимодействовал с ними обоими
        // отсортированные в убывающем порядке по коэффициенту схожести
        List<Similarity> similarities = similarityService.findNPairContainsEventIdsSortedDescScore(actionIds, request.getMaxResults());

        // выберем уникальные мероприятия из пар, с которыми пользователь не взаимодействовал
        // и оставим максимальный коэффициент схожести
        Map<Long, Double> eventIds = similarities.stream()
                .collect(Collectors.toMap(o -> actionIds.contains(o.getKey().getEventId()) ? o.getKey().getOtherEventId() : o.getKey().getEventId(),
                        Similarity::getScore, Double::max));

        return eventIds.entrySet().stream().map(o -> RecommendedEventProto.newBuilder()
                .setEventId(o.getKey())
                .setScore(o.getValue())
                .build()).toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        // получим ID мероприятий
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        // выберем из БД все мероприятия из этого списка IDs
        Map<Long, Double> actionMap = actionService.findAllByEventIds(eventIds).stream()
                .collect(Collectors.groupingBy(Action::getEventId,
                        Collectors.summingDouble(o -> getUserActionWeight(o.getActionType()))));

        return actionMap.entrySet().stream().map(o -> RecommendedEventProto.newBuilder()
                .setEventId(o.getKey())
                .setScore(o.getValue())
                .build()).toList();
    }

    private double getUserActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            case LIKE -> userActionWeightConfig.getLIKE();
        };
    }
}
