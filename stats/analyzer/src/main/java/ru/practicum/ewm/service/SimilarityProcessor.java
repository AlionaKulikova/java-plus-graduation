package ru.practicum.ewm.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.configuration.KafkaConfig;
import ru.practicum.ewm.configuration.KafkaTopic;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.ewm.model.SimilarityCompositeKey;
import ru.practicum.ewm.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class SimilarityProcessor implements Runnable {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private final KafkaConsumer<Long, EventSimilarityAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final SimilarityRepository repository;

    public SimilarityProcessor(KafkaConfig kafkaConfig, SimilarityRepository repository) {
        consumer = new KafkaConsumer<>(kafkaConfig.getSimilarityConsumerProps());
        topics = kafkaConfig.getTopics();
        this.repository = repository;
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.EVENTS_SIMILARITY)));

        // регистрируем хук при завершении JVM
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            // Цикл обработки событий
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    log.info("Received similarity: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());

                    // обновим информацию в БД о сходствах мероприятий
                    Similarity similarity = new Similarity();
                    similarity.setKey(new SimilarityCompositeKey(record.value().getEventA(), record.value().getEventB()));
                    similarity.setScore(record.value().getScore());
                    repository.save(similarity);
                    log.info("Similarity has been processed.");
                }

                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Error during offset fixing. Offset: {}", offsets, exception);
                    }
                });
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер в блоке finally
        } catch (Exception e) {
            log.error("Error during processing of events from sensors", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing the consumer");
                consumer.close();
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }
}
