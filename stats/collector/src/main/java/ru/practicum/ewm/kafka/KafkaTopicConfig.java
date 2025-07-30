package ru.practicum.ewm.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Getter
@Setter
@Component
@ConfigurationProperties("collector.kafka")
public class KafkaTopicConfig {
    EnumMap<KafkaTopic, String> topics = new EnumMap<>(KafkaTopic.class);
}
