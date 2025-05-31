package com.linyajin.mikufans.handler;

import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@Order(-1)
public class GatewayExceptionHandler implements WebExceptionHandler {

    private static final String STATUC_SUCCESS = "success";

    private static final String STATUC_ERROR = "error";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ResponseVO responseVO = getResponse(ex);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);


        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.covertObj2Json(responseVO).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private ResponseVO getResponse(Throwable throwable) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(STATUC_ERROR);
        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) throwable;
            if (HttpStatus.NOT_FOUND == responseStatusException.getStatusCode()) {
                //当服务返回 404 Not Found 时（如请求的接口不存在），直接返回 404 信息
                responseVO.setCode(ResponseCodeEnum.CODE_404.getCode());
                responseVO.setInfo(ResponseCodeEnum.CODE_404.getMsg());
                return responseVO;

            } else if(HttpStatus.SERVICE_UNAVAILABLE == responseStatusException.getStatusCode()){
                //当服务返回 503 Service Unavailable 时（如服务不可用或过载）
                responseVO.setCode(ResponseCodeEnum.CODE_503.getCode());
                responseVO.setInfo(ResponseCodeEnum.CODE_503.getMsg());
                return responseVO;
            } else {
                responseVO.setCode(responseStatusException.getStatusCode().value());
                responseVO.setInfo(ResponseCodeEnum.CODE_503.getMsg());
                return responseVO;
            }
        } else if (throwable instanceof BusinessException) {
            BusinessException businessException = (BusinessException) throwable;
            responseVO.setCode(businessException.getCode());
            responseVO.setInfo(businessException.getMessage());
            return responseVO;
        }

        responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        return responseVO;
    }
}
