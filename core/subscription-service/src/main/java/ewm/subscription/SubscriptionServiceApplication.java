package ewm.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ewm.interaction.feign")
public class SubscriptionServiceApplication {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }
}