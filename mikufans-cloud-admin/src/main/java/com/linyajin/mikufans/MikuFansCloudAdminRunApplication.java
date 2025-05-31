package com.linyajin.mikufans;

import org.apache.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.linyajin.mikufans.mappers")
@EnableDiscoveryClient
@EnableFeignClients
public class MikuFansCloudAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(MikuFansCloudAdminRunApplication.class, args);
    }
}
