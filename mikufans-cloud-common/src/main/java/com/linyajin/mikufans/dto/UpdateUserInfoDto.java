package com.linyajin.mikufans.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserInfoDto {
    @NotEmpty(message = "昵称不能为空")
    @Size(max = 20)
    private String nickName;
    @NotEmpty(message = "头像不能为空")
    @Size(max = 100)
    private String avatar;
    @NotNull(message = "性别不能为空")
    private Integer sex;
    @Size(max = 10)
    private String birthday;
    @Size(max = 15)
    private String school;
    @Size(max = 200)
    private String personIntroduction;
    @Size(max = 50)
    private String noticeInfo;

}
