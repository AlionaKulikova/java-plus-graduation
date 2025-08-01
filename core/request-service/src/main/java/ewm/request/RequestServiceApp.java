package ewm.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {"ewm.interaction.feign"})
@ComponentScan(basePackages = {"ewm.client", "ewm.request", "ru.practicum.ewm.stats.client"})
public class RequestServiceApp {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}