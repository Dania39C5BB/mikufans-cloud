package com.linyajin.mikufans.fallback;

import com.linyajin.mikufans.api.consumer.WebClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
@Slf4j
public class WebClientFallbackFactory implements FallbackFactory<WebClient> {
    @Override
    public WebClient create(Throwable cause) {
        log.error("Fallback triggered for exception: ", cause);
        // 使用动态代理处理所有方法
        return (WebClient) Proxy.newProxyInstance(
                WebClient.class.getClassLoader(),
                new Class<?>[] { WebClient.class },
                (proxy, method, args) -> {
                    // 统一降级逻辑
                    if (cause instanceof FeignException.ServiceUnavailable) {
                        return "Fallback: 服务不可用 (503) - 方法: " + method.getName();
                    }
                    return "Fallback: 未知错误 (" + cause.getMessage() + ") - 方法: " + method.getName();
                }
        );
    }
}
