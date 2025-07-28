package ru.practicum.ewm.stats.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class KafkaConfig {

    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {//настройки продюсера
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);//erl
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);//сеарелизация ключа
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);//сереализация значения

        return new KafkaProducer<>(props);
    }
}
