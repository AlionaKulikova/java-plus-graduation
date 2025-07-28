package ru.practicum.stats.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorSimilarityService {
//ф-я отправки в брокер для дальнейшего получения аналайзером
    private final KafkaProducer<String, Object> kafkaProducer;
    @Value("${kafka.topics.similarity}")
    private String similarityTopic;//создаем топик для отпраки в брокер

    public void sendEventSimilarity(EventSimilarityAvro eventSimilarity) {
        //создаем запись, сериализуем в байты и отпраляем в брокер , ловим ошибки или выводим в консоль успех
        ProducerRecord<String, Object> record = new ProducerRecord<>(similarityTopic, eventSimilarity);
        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.info("Сообщение отправлено: {}", metadata.toString());
            } else {
                exception.printStackTrace();
            }
        });
    }

}