package ru.practicum.stats.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final AggregatorSimilarityService aggregatorSimilarityService;
    private final KafkaConsumer<String, UserActionAvro> kafkaConsumer;
    private Map<Long, Map<Long, Double>> weights = Collections.synchronizedMap(new HashMap<>());
    private Map<Long, Map<Long, Double>> sMinMap = Collections.synchronizedMap(new HashMap<>());

    private static final Double VIEW_WEIGHT = 0.4;
    private static final Double REGISTER_WEIGHT = 0.8;
    private static final Double LIKE_WEIGHT = 1.0;

    @Value("${kafka.topics.actions}")
    private String topic;

    public void start() {
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, UserActionAvro> records = kafkaConsumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, UserActionAvro> record : records) {
                UserActionAvro userAction = record.value();
                log.info("Принято сообщение {}", userAction);


                double weight = 0.0;
                switch (userAction.getActionType()) {
                    case VIEW -> {
                        weight = VIEW_WEIGHT;
                    }
                    case REGISTER -> {
                        weight = REGISTER_WEIGHT;
                    }
                    case LIKE -> {
                        weight = LIKE_WEIGHT;
                    }
                }

                double oldEventAWeight = 0;
                double newEventAWeight = weight;
                if (weights.get(userAction.getEventId()) == null
                        || weights.get(userAction.getEventId()).get(userAction.getUserId()) == null) {
                    Map<Long, Double> hashMap = Collections.synchronizedMap(new HashMap<>());
                    weights.put(userAction.getEventId(), hashMap);
                    weights.get(userAction.getEventId()).putIfAbsent(userAction.getUserId(), 0.0);
                } else {
                    oldEventAWeight = weights
                            .get(userAction.getEventId())
                            .get(userAction.getUserId());

                    newEventAWeight = Math.max(weights
                            .get(userAction.getEventId())
                            .get(userAction.getUserId()), weight);
                }

                long eventAId = userAction.getEventId();
                if (newEventAWeight > oldEventAWeight) {
                    for (Long eventBId : weights.keySet()) {
                        if (weights.get(eventBId).get(userAction.getUserId()) != null && eventBId != eventAId) {
                            double sMin = getMinWeightSumForAllUsers(eventAId, eventBId);
                            double sAOld = getWeightSumForAllUsers(eventAId);
                            double sBOld = getWeightSumForAllUsers(eventBId);

                            double minOld = Math.min(
                                    oldEventAWeight,
                                    weights.get(eventBId).get(userAction.getUserId()));

                            double minNew = Math.min(
                                    newEventAWeight,
                                    weights.get(eventBId).get(userAction.getUserId()));
                            double deltaMin = minNew - minOld;

                            double sMinNew = sMin + deltaMin;
                            if (deltaMin != 0) {
                                put(eventAId, eventBId, sMinNew);
                            }

                            double deltaWeightA = newEventAWeight
                                    - weights.get(eventAId).get(userAction.getUserId());
                            double sAnew = sAOld + deltaWeightA;

                            double similarity = sMinNew / (Math.sqrt(sAnew) * Math.sqrt(sBOld));

                            EventSimilarityAvro eventSimilarityAvro = new EventSimilarityAvro(
                                    eventAId,
                                    eventBId,
                                    similarity,
                                    userAction.getTimestamp());
                            aggregatorSimilarityService.sendEventSimilarity(eventSimilarityAvro);
                        }
                    }
                }
                weights.get(eventAId).put(userAction.getUserId(), newEventAWeight);

            }
            kafkaConsumer.commitSync();
        }
    }

    private Double getMinWeightSumForAllUsers(Long eventAId, Long eventBId) {
        double sum = 0.0;
        for (Long uid : weights.get(eventAId).keySet()) {
            double firstWeight = 0;
            if (weights.get(eventAId).get(uid) != null) {
                firstWeight = weights.get(eventAId).get(uid);
            }
            double secondWeight = 0;
            if (weights.get(eventBId).get(uid) != null) {
                secondWeight = weights.get(eventBId).get(uid);
            }
            sum += Math.min(firstWeight, secondWeight);
        }
        return sum;
    }

    private Double getWeightSumForAllUsers(Long eventAId) {
        Double sum = 0.0;
        for (Long uid : weights.get(eventAId).keySet()) {
            sum += weights.get(eventAId).get(uid);
        }
        return sum;
    }

    public void put(long eventA, long eventB, Double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        sMinMap
                .computeIfAbsent(first, e -> Collections.synchronizedMap(new HashMap<>()))
                .put(second, sum);
    }
}

