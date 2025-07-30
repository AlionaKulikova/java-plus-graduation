package ru.practicum.ewm.service.similarity;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.configuration.KafkaConfig;
import ru.practicum.ewm.configuration.KafkaTopic;
import ru.practicum.ewm.model.similarity.Similarity;
import ru.practicum.ewm.model.similarity.SimilarityCompositeKey;
import ru.practicum.ewm.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class SimilarityProcessor implements Runnable {
    private final SimilarityRepository similarityRepository;
    private final EnumMap<KafkaTopic, String> topics;
    private final KafkaConsumer<Long, EventSimilarityAvro> consumer;

    public SimilarityProcessor(KafkaConfig kafkaConfig, SimilarityRepository repository) {
        this.similarityRepository = repository;
        topics = kafkaConfig.getTopics();
        consumer = new KafkaConsumer<>(kafkaConfig.getSimilarityConsumerProps());
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.EVENTS_SIMILARITY)));

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(5000));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    Similarity similarity = new Similarity();
                    similarity.setKey(new SimilarityCompositeKey(record.value().getEventA(), record.value().getEventB()));
                    similarity.setScore(record.value().getScore());
                    similarityRepository.save(similarity);
                }
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка при фиксации смещений: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
            // Остановка потребителя
        } catch (Exception error) {
            log.error("Ошибка при обработке действий пользователя", error);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }
}
