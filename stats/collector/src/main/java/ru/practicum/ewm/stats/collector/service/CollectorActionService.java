package ru.practicum.ewm.stats.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorActionService {

    private final KafkaProducer<String, Object> kafkaProducer;
    @Value("${kafka.topics.actions}")
    private String actionsTopic;

    public void sendUserAction(UserActionAvro userAction) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(actionsTopic, userAction);
        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.info("Сообщение отправлено: {}", metadata.toString());
            } else {
                exception.printStackTrace();
            }
        });
    }

}