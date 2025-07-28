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

    public void sendUserAction(UserActionAvro userAction) {//принимаем на вход передаваемое сообщение
        ProducerRecord<String, Object> record = new ProducerRecord<>(actionsTopic, userAction);//записываем его а топик
        kafkaProducer.send(record, (metadata, exception) -> {// callback (обратный вызов), который вызывается, когда отправка завершена или произошла ошибка.
            if (exception == null) {//если ошибок нет
                log.info("Сообщение отправлено: {}", metadata.toString());//объект RecordMetadata, содержащий информацию о записи, например, топик, партицию, оффсет, время
            } else {//Выводим стек ошибки
                exception.printStackTrace();
            }
        });
    }

}