package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.service.UserActionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/online")
@Validated
public class OnlineController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取视频在线播放人数
     * @param deviceId 设备id 浏览器唯一标识
     * @param fileId 视频id
     * @return ResponseVO
     */
    @GetMapping("/reportVideoPlayOneLine")
    public ResponseVO reportVideoPlayOneLine(@NotEmpty String deviceId , @NotEmpty String fileId) {
        return getSuccessResponseVO(redisComponent.reportVideoPlayOneLine(fileId , deviceId));
    }

}
