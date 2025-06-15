package com.linyajin.mikufans;

import org.apache.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@MapperScan("com.linyajin.mikufans.mappers")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableTransactionManagement
public class MikuFansCloudWebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(MikuFansCloudWebRunApplication.class, args);
    }
}
