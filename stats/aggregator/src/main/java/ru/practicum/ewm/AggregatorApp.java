package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.ewm.controller.AggregationStarter;

@EnableDiscoveryClient
@ConfigurationPropertiesScan
@SpringBootApplication
public class AggregatorApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApp.class, args);
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}