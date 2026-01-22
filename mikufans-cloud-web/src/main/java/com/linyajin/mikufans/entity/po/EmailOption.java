package com.linyajin.mikufans.entity.po;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 邮件请求实体类
 */
@Data
public class EmailOption  {
    // 发件人邮箱地址
    @NotEmpty(message = "邮箱地址不能为空")
    private String emailAccount;
    // 发件人邮箱授权码
    @NotEmpty(message = "邮箱授权码不能为空")
    private String emailPassword;
    // 发件内容
    @NotEmpty(message = "发件内容不能为空")
    private String content;
    // 发件人昵称
    @NotEmpty(message = "发件人昵称不能为空")
    private String sendUserName;
    //收件人邮箱地址
    @NotEmpty(message = "收件人邮箱地址不能为空")
    private String receiveEmailAccount;
    //邮件标题
    @NotEmpty(message = "邮件标题不能为空")
    private String emailTitle;
    //附件文件
//    private MultipartFile file;

}
