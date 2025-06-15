package com.linyajin.mikufans;

import org.apache.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MikuFansCloudGatewayRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(MikuFansCloudGatewayRunApplication.class, args);
    }
}
