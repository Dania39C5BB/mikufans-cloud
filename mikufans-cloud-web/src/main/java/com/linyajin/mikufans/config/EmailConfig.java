package com.linyajin.mikufans.config;

import lombok.Data;

/**
 * 邮件配置实体类
 */
@Data
public class EmailConfig {
    // 邮件协议
    private String protocol = "smtp";
    // SMTP服务器地址
    private String host = "smtp.qq.com";
    // 端口
    private String port = "465";
    // 发件人邮箱地址
    private String account;
    // 发件人邮箱授权码
    private String password;
    // 发件人名称
    private String senderName = "系统邮件";
    // 是否启用SSL
    private Boolean sslEnabled = true;
    // 连接超时时间(ms)
    private Integer connectionTimeout = 10000;
    // 读取超时时间(ms)
    private Integer timeout = 10000;
    // 写入超时时间(ms)
    private Integer writeTimeout = 10000;
    // 是否开启调试模式
    private Boolean debug = false;

    public EmailConfig() {}

    public EmailConfig(String account, String password) {
        this.account = account;
        this.password = password;
    }
}