package br.com.infnet.passageiros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@org.springframework.boot.autoconfigure.domain.EntityScan("br.com.infnet")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories("br.com.infnet")
@SpringBootApplication(scanBasePackages = "br.com.infnet")
@EnableDiscoveryClient
public class PassageirosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassageirosApplication.class, args);
    }
}
