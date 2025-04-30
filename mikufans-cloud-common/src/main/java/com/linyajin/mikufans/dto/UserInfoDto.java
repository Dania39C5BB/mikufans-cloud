package com.linyajin.mikufans.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserInfoDto {
    @NotEmpty(message = "昵称不能为空")
    private String nickName;
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 20, message = "邮箱长度不能超过20")
    private String email;
    @NotEmpty(message = "密码不能为空")
    private String password;
    @NotEmpty(message = "验证码不能为空")
    private String checkCode;
    @NotEmpty(message = "验证码key不能为空")
    private String checkCodeKey;
}
