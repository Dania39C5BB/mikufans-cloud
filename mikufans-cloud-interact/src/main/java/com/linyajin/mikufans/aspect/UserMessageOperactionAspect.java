package com.linyajin.mikufans.aspect;

import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.service.UserMessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component
@Aspect
@Slf4j
public class UserMessageOperactionAspect {


    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserMessageService userMessageService;

    /**
     * 环绕通知，拦截带有 @RecordUserMessage 注解的方法
     * @param joinPoint 连接点，可以获取方法信息和参数
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(com.linyajin.mikufans.annotation.RecordUserMessage)")
    public ResponseVO around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行目标方法并获取返回值
        ResponseVO responseVO = (ResponseVO)joinPoint.proceed();
        // 获取被拦截方法的信息
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取方法上的 @RecordUserMessage 注解信息
        RecordUserMessage recordUserMessage = method.getAnnotation(RecordUserMessage.class);

        //joinPoint.getArgs()	Object[]	方法调用时传入的实际参数值（如用户ID、操作内容等）
        //method.getParameters()	Parameter[]	方法的参数定义（包含参数名、类型等元信息）
        saveMessage(recordUserMessage , joinPoint.getArgs(),method.getParameters());


        return responseVO;
    }

    private void saveMessage(RecordUserMessage recordUserMessage , Object[] args , Parameter[] parameters) {
        String videoId = null;
        Integer actionType = null;
        String content = null;
        Integer replyCommentId = null;
        for (int i = 0; i < parameters.length; i++) {
            if ("videoId".equals(parameters[i].getName())) {
                videoId = (String) args[i];
            }
            if ("actionType".equals(parameters[i].getName())) {
                actionType = (Integer) args[i];
            }
            if ("content".equals(parameters[i].getName())) {
                content = (String) args[i];
            }
            if ("replyCommentId".equals(parameters[i].getName())) {
                replyCommentId = (Integer) args[i];
            }
            if ("reason".equals(parameters[i].getName())) {
                content = (String) args[i];
            }
        }
        //从注解获取操作类型
        MessageTypeEnum messageTypeEnum = recordUserMessage.messageType();
        if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(actionType)) {
            messageTypeEnum = MessageTypeEnum.COLLECTION;
        }

        //获取操作人
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String sendUserId = tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId();

        userMessageService.saveUserMessage(videoId, sendUserId, messageTypeEnum ,content, replyCommentId);

    }

    private TokenUserInfoDto getTokenUserInfoDto() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenUserInfo(token);
    }
}
