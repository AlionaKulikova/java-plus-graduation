package ru.practicum.ewm.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.configuration.KafkaConfig;
import ru.practicum.ewm.configuration.KafkaTopic;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.ActionType;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class ActionProcessor implements Runnable {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(5000);
    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final EnumMap<KafkaTopic, String> topics;
    private final ActionService actionService;

    public ActionProcessor(UserActionWeightConfig userActionWeightConfig, KafkaConfig kafkaConfig, ActionService actionService) {
        consumer = new KafkaConsumer<>(kafkaConfig.getActionConsumerProps());
        topics = kafkaConfig.getTopics();
        this.actionService = actionService;
    }

    @Override
    public void run() {
        log.info("Starting History Processor");
        consumer.subscribe(List.of(topics.get(KafkaTopic.USER_ACTIONS)));

        // регистрируем хук при завершении JVM
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            // Цикл обработки событий
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    log.info("Received user action: topic = {}, partition = {}, offset = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.value());

                    // сохраним историю действий пользователя в БД если изменился вес действия в большую сторону
                    actionService.addAction(record.value().getUserId(), record.value().getEventId(),
                            ActionType.valueOf(record.value().getActionType().name()),
                            record.value().getTimestamp());

                    log.info("History has been processed.");
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
