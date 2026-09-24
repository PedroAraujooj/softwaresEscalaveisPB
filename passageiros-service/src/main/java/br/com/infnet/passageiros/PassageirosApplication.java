package br.com.infnet.passageiros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PassageirosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassageirosApplication.class, args);
    }
}
