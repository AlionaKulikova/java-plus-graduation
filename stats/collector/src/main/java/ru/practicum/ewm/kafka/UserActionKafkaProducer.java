package ru.practicum.ewm.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaProducer {
    private final KafkaTopicConfig topicConfig;
    private final KafkaTemplate<Long, SpecificRecordBase> producer;

    public void send(UserActionAvro userAction, KafkaTopic topic) {
        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(
                        topicConfig.getTopics().get(topic),
                        null,
                        userAction.getTimestamp().toEpochMilli(),
                        userAction.getEventId(),
                        userAction);

        CompletableFuture<SendResult<Long, SpecificRecordBase>> sendCompletion = producer.send(record);

        sendCompletion.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Сообщение отправлено");
            } else {
                log.error("Ошибка при отправке сообщения в Kafka", exception);
            }
        });
    }

    @PreDestroy
    public void close() {
        producer.flush();
        producer.destroy();
    }
}