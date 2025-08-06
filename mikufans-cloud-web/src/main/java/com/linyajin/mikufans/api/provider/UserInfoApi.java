package com.linyajin.mikufans.api.provider;


import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/innerApi/user")
@Validated
public class UserInfoApi {

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/updateCoinCount")
    public Integer updateCoinCount(@NotEmpty  String userId , @NotNull Integer count) {
        return userInfoService.updateCoinCount(userId ,count);
    }

    @GetMapping("/selectByUserId")
    public UserInfo selectByUserId(@NotEmpty  String userId){
        return userInfoService.getUserInfoByUserId(userId);
    }
}
