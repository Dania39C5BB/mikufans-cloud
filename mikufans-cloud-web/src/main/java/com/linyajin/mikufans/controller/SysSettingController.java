package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.redis.RedisComponent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysSetting")
public class SysSettingController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(SysSettingController.class);
    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;


    //获取系统设置信息
    @GetMapping("/getSysSetting")
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }


}
