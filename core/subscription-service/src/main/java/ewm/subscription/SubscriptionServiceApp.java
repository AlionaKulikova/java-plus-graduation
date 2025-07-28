package ewm.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableFeignClients(basePackages = "ewm.interaction.feign")
public class SubscriptionServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApp.class, args);
    }
}