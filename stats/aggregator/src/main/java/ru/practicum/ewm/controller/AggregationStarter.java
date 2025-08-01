package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.configuration.KafkaConfig;
import ru.practicum.ewm.configuration.KafkaTopic;
import ru.practicum.ewm.repository.AggregatorRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AggregationStarter {
    private final KafkaProducer<Long, SpecificRecordBase> producer;
    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final AggregatorRepository repository;

    private final Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<>();
    private static final int OFFSET_COMMIT_BATCH_SIZE = 10;

    public AggregationStarter(KafkaConfig kafkaConfig, AggregatorRepository aggregatorRepository) {
        topics = kafkaConfig.getTopics();
        producer = new KafkaProducer<>(kafkaConfig.getProducerProps());
        consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProps());
        repository = aggregatorRepository;
    }

    public void start() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.USER_ACTIONS)));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        int processedCount = 0;

        try {
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(Duration.ofMillis(5000));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    List<EventSimilarityAvro> similarities = repository.updateEventSimilarity(record.value());

                    for (EventSimilarityAvro similarity : similarities) {
                        sendEventSimilarity(similarity);
                    }

                    processedCount++;
                    updateOffsets(record, processedCount);
                }
            }
        } catch (WakeupException ignored) {
            // Остановка потребителя
        } catch (Exception error) {
            log.error("Ошибка при обработке действий пользователя", error);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close();
            }
        }
    }

    private void sendEventSimilarity(EventSimilarityAvro similarity) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                topics.get(KafkaTopic.EVENTS_SIMILARITY),
                null,
                similarity.getTimestamp().toEpochMilli(),
                similarity.getEventA(),
                similarity);
        producer.send(record);
    }

    private void updateOffsets(ConsumerRecord<Long, UserActionAvro> record, int count) {
        offsetMap.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % OFFSET_COMMIT_BATCH_SIZE == 0) {
            consumer.commitAsync(offsetMap, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка при фиксации смещений: {}", offsets, exception);
                }
            });
        }
    }
}