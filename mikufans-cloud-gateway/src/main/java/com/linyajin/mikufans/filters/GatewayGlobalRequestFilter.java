package com.linyajin.mikufans.filters;

import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayGlobalRequestFilter implements GlobalFilter , Ordered {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String rawPath = exchange.getRequest().getURI().getRawPath();

        if (rawPath.indexOf("innerApi") != -1) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        log.info("请求路径:{}", rawPath);
        return chain.filter(exchange);
    }
}
