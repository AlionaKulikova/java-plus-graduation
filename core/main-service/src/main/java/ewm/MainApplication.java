package ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"ru.practicum", "ewm.client"})
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ewm.client")  // Explicitly specify Feign clients package
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}