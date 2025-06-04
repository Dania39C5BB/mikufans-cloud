package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.dto.SysSettingDto;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.redis.RedisComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysSetting")
@Slf4j
public class SysSettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;


    //获取系统设置信息
    @GetMapping("/getSysSetting")
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }


    //修改系统设置信息
    @PutMapping("/updateSysSetting")
    public ResponseVO updateSysSetting(SysSettingDto sysSettingDto) {

        redisComponent.saveSysSettingDto(sysSettingDto);

        return getSuccessResponseVO(null);
    }

}
