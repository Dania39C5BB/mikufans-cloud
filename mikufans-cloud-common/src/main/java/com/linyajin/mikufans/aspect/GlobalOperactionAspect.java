package com.linyajin.mikufans.aspect;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisUtils;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 切面类，用于全局操作拦截
 */

@Aspect
@Component
@Slf4j
public class GlobalOperactionAspect {

    @Resource
    private RedisUtils redisUtils;

    //拦截带有GlobalInterceptor注解的方法
    //切入点表达式，表示拦截所有带有 @GlobalInterceptor 注解的方法。
    @Before("@annotation(com.linyajin.mikufans.annotation.GlobalInterceptor)")
    //切点拦截方法执行前的操作
    //JoinPoint point：提供被拦截方法的信息（如方法名、参数等）。
    public void InterceptorBefore(JoinPoint point) {
//      MethodSignature：获取被拦截方法的签名信息。
//      getMethod()：获取具体的 Method 对象。
        Method method = ((MethodSignature)point.getSignature()).getMethod();
//      getAnnotation(GlobalInterceptor.class)：检查方法是否带有 @GlobalInterceptor 注解。
        GlobalInterceptor Interceptor = method.getAnnotation(GlobalInterceptor.class);
        if (null == Interceptor) {
            return;
        }
        if (Interceptor.checkLogin()) {
            checkLogin();
            log.info("拦截到需要登录的操作");
        }
    }
    private void checkLogin() {
        // 1. 获取当前 HTTP 请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 2. 从请求头获取 Token
        String token = request.getHeader(Constants.TOKEN_WEB);
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        // 3. 从 Redis 查询 Token 对应的用户信息
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto)redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }
}
