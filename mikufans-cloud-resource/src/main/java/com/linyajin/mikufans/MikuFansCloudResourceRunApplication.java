package com.linyajin.mikufans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.linyajin.mikufans" , exclude = {
        DataSourceAutoConfiguration.class
})
@EnableFeignClients
@EnableDiscoveryClient
public class MikuFansCloudResourceRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(MikuFansCloudResourceRunApplication.class, args);
    }
}
