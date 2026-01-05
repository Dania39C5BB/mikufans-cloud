package com.linyajin.mikufans.config;

import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class FeignMultipartConfig {

    /**
     * 正确的Feign编码器配置：层级清晰，类型匹配
     */
    @Bean
    public SpringFormEncoder feignFormEncoder() {
        // 步骤1：获取Spring默认的消息转换器列表
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();

        // 步骤2：将List包装为HttpMessageConverters（解决之前的类型错误）
        HttpMessageConverters httpMessageConverters = new HttpMessageConverters(converterList);

        // 步骤3：创建SpringEncoder（参数是HttpMessageConverters的Supplier）
        SpringEncoder springEncoder = new SpringEncoder(() -> httpMessageConverters);

        // 步骤4：创建SpringFormEncoder（参数是SpringEncoder），最终返回Encoder类型
        return new SpringFormEncoder(springEncoder);
    }
}
