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
import ru.practicum.ewm.dao.AggregatorRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
public class AggregationStarter {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final int COUNT_FIX_OFFSETS = 10; // кол-во офсетов для фиксации за раз
    private final KafkaProducer<Long, SpecificRecordBase> producer;
    private final KafkaConsumer<Long, ru.practicum.ewm.stats.avro.UserActionAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final AggregatorRepository repository;

    public AggregationStarter(KafkaConfig kafkaConfig, AggregatorRepository aggregatorRepository) {
        topics = kafkaConfig.getTopics();
        producer = new KafkaProducer<>(kafkaConfig.getProducerProps());
        consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProps());
        repository = aggregatorRepository;
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топик для получения событий по действиям пользователей,
     * рассчитывает сходства мероприятий и сохраняет результаты в Kafka для последующей обработки
     */
    public void start() {
        log.info("Starting aggregator...");
        try {
            // подписка на топик
            consumer.subscribe(List.of(topics.get(KafkaTopic.USER_ACTIONS)));

            // регистрируем хук при завершении JVM
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                int count = 0;
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    log.info("Received record: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    List<EventSimilarityAvro> eventSimilarityAvros = repository.updateEventSimilarity(record.value());

                    for (EventSimilarityAvro eventSimilarity : eventSimilarityAvros) {
                        ProducerRecord<Long, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                                topics.get(KafkaTopic.EVENTS_SIMILARITY),
                                null,
                                eventSimilarity.getTimestamp().toEpochMilli(),
                                eventSimilarity.getEventA(),
                                eventSimilarity);
                        producer.send(producerRecord);
                        manageOffsets(record, count, consumer);
                        log.info("Similarity for event ID {} and ID {} sent to topic {}", eventSimilarity.getEventA(),
                                eventSimilarity.getEventB(), producerRecord.topic());
                        count++;
                    }
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Error during processing of user actions", e);
        } finally {

            try {
                // перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буфере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // вызываем метод продюсера для сброса данных в буфере
                producer.flush();
                // вызываем метод  консьюмера для фиксации смещений
                consumer.commitSync();

            } finally {
                log.info("Closing the consumer");
                consumer.close();
                log.info("Closing the producer");
                producer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Long, UserActionAvro> record, int count, KafkaConsumer<Long, UserActionAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % COUNT_FIX_OFFSETS == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error during offset fixing: {}", offsets, exception);
                }
            });
        }
    }
}
