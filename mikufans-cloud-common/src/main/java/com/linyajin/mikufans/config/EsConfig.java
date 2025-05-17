package com.linyajin.mikufans.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class EsConfig {
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        // 1. 创建底层 REST 客户端
//        RestClient restClient = RestClient.builder(
//                new HttpHost("localhost", 9200) // 替换为你的ES地址
//        ).build();
//
//        // 2. 使用Jackson映射器创建传输层
//        ElasticsearchTransport transport = new RestClientTransport(
//                restClient,
//                new JacksonJsonpMapper()
//        );
//
//        // 3. 返回API客户端
//        return new ElasticsearchClient(transport);
//    }
//}
