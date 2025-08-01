package ru.practicum.ewm.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Properties;

@Getter
@Setter
@Component
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    private EnumMap<KafkaTopic, String> topics = new EnumMap<>(KafkaTopic.class);
    private Properties producerProps;
    private Properties consumerProps;
}