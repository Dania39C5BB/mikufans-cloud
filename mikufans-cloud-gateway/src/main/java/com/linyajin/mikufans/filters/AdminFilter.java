package com.linyajin.mikufans.filters;

import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.utils.StringTools;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class AdminFilter extends AbstractGatewayFilterFactory {

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            //TODO: 管理端必须登录才行

            // 获取当前请求对象
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求的原始路径
            String rawPath = request.getURI().getRawPath();
            // 检查路径是否包含 "/account"
            if (rawPath.contains("/account")) {
                return chain.filter(exchange); // 如果包含，继续执行过滤器链
            }

            String token = getToken(request);

            if (rawPath.contains("/file")) {
                token = getTokenFromCookie(request);
            }

            if (StringTools.isEmpty(token)) {
                throw new BusinessException(ResponseCodeEnum.CODE_901);
            }

            return chain.filter(exchange);
        });
    }

    //获取token
    private String getToken(ServerHttpRequest  request) {
        return request.getHeaders().getFirst(Constants.TOKEN_ADMIN);
    }

    //从cookie中获取token
    private String getTokenFromCookie(ServerHttpRequest  request) {
        return request.getCookies().getFirst(Constants.TOKEN_ADMIN).getValue();
    }


}
