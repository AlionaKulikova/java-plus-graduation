package ru.practicum.ewm.stats.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.Action;
import ru.practicum.ewm.stats.analyzer.model.Similarity;
import ru.practicum.ewm.stats.analyzer.repository.ActionRepository;
import ru.practicum.ewm.stats.analyzer.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerStarter {
    private final KafkaConsumer<String, EventSimilarityAvro> similarityKafkaConsumer;
    private final KafkaConsumer<String, UserActionAvro> actionsKafkaConsumer;

    private final SimilarityRepository similarityRepository;
    private final ActionRepository actionRepository;
//будем подписываться на 2 топика. Один из агрегатораkafka.topics.similarity
    @Value("${kafka.topics.similarity}")
    private String similarityTopic;
//второй из коллектора?????
    @Value("${kafka.topics.actions}")
    private String actionsTopic;

    public void start() {
        similarityKafkaConsumer.subscribe(Collections.singletonList(similarityTopic));
        actionsKafkaConsumer.subscribe(Collections.singletonList(actionsTopic));

        while (true) {
            ConsumerRecords<String, EventSimilarityAvro> similarityRecords = similarityKafkaConsumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, EventSimilarityAvro> record : similarityRecords) {
                EventSimilarityAvro eventSimilarityAvro = record.value();
                log.info("Принято сообщение {}", eventSimilarityAvro);
                Similarity similarity = new Similarity();
                similarity.setEventaId(eventSimilarityAvro.getEventA());
                similarity.setEventbId(eventSimilarityAvro.getEventB());
                similarity.setScore(eventSimilarityAvro.getScore());
                similarity.setTimestamp(eventSimilarityAvro
                        .getTimestamp()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());

                log.info("В БД сохранено {}", similarityRepository.save(similarity));
            }
            similarityKafkaConsumer.commitSync();

            ConsumerRecords<String, UserActionAvro> actionRecords = actionsKafkaConsumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, UserActionAvro> record : actionRecords) {
                UserActionAvro userActionAvro = record.value();
                log.info("Принято сообщение {}", userActionAvro);
                Action action = new Action();
                action.setEventId(userActionAvro.getEventId());
                action.setUserId(userActionAvro.getUserId());
                action.setAction(userActionAvro.getActionType().toString());
                action.setTimestamp(userActionAvro
                        .getTimestamp()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());

                log.info("В БД сохранено {}", actionRepository.save(action));
            }
            actionsKafkaConsumer.commitSync();
        }
    }

}

