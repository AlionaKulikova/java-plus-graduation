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
    private final KafkaTemplate<Long, SpecificRecordBase> producer;
    private final KafkaTopicConfig topicConfig;

    public void send(UserActionAvro userAction, KafkaTopic topic) {
        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(
                        topicConfig.getTopics().get(topic),
                        null,
                        userAction.getTimestamp().toEpochMilli(),
                        userAction.getEventId(),
                        userAction);

        log.info("Sending user action {} at event ID {} to topic {}", userAction.getClass().getSimpleName(), userAction.getEventId(), topic);
        CompletableFuture<SendResult<Long, SpecificRecordBase>> future = producer.send(record);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User action {} at event ID {} has been sent to the topic {}", userAction.getClass().getSimpleName(), userAction.getEventId(), topic);
            } else {
                log.error("Error sending the user action {}, error: {}", userAction.getClass().getSimpleName(), ex.getMessage());
            }
        });
    }

    @PreDestroy
    public void close() {
        log.info("Shutting down producer");
        producer.flush();
        producer.destroy();
    }
}
