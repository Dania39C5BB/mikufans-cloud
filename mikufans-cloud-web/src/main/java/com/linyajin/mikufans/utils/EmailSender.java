package com.linyajin.mikufans.utils;


import com.linyajin.mikufans.entity.po.EmailOption;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 邮件发送工具类
 * 支持：发送文本邮件、HTML邮件、带附件邮件、带MultipartFile附件邮件
 */
@Slf4j
@Component
public class EmailSender {
    // 邮件协议
    private static final String emailProtocol = "smtp";
    // 发件人的SMTP服务器地址
    private static final String emailSMTPHost = "smtp.qq.com";
    // 端口
    private static final String emailPort = "465";

    /**
     * 发送邮件
     * @param emailOption 邮件配置信息
     * @param emails  目标邮件地址集合
     */
    public static boolean sendEmail(EmailOption emailOption, Set<String> emails) {
        // 未传收件人邮箱地址则直接返回
        if (emails == null || emails.isEmpty()) return false;
        try {
            // 创建参数配置, 用于连接邮件服务器的参数配置
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", emailProtocol); // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", emailSMTPHost); // 指定smtp服务器地址
            props.setProperty("mail.smtp.port", emailPort); // 指定smtp端口号
            // 使用smtp身份验证
            props.setProperty("mail.smtp.auth", "true"); // 需要请求认证
            props.put("mail.smtp.ssl.enable", "true"); // 开启SSL
            props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // 指定SSL版本
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            // 由于Properties默认不限制请求时间，可能会导致线程阻塞，所以指定请求时长
            props.setProperty("mail.smtp.connectiontimeout", "10000");// 与邮件服务器建立连接的时间限制
            props.setProperty("mail.smtp.timeout", "10000");// 邮件smtp读取的时间限制
            props.setProperty("mail.smtp.writetimeout", "10000");// 邮件内容上传的时间限制
            // 根据配置创建会话对象, 用于和邮件服务器交互
            Session session = Session.getDefaultInstance(props);
            session.setDebug(false); // 设置为debug模式, 可以查看详细的发送log
            // 创建邮件
            MimeMessage message = new MimeMessage(session);
            // From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
            message.setFrom(new InternetAddress(emailOption.getEmailAccount(), emailOption.getSendUserName(), "UTF-8"));
            // To: 收件人（可以增加多个收件人、抄送、密送）
            // MimeMessage.RecipientType.TO: 发送 MimeMessage.RecipientType.CC：抄送 MimeMessage.RecipientType.BCC：密送
            int size = emails.size();
            // 单个目标邮箱还是多个
            if (size == 1) {
                String email = emails.iterator().next();
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, email, "UTF-8"));
            } else {
                InternetAddress[] addresses = new InternetAddress[emails.size()];
                int i = 0;
                for (String email : emails) {
                    addresses[i++] = new InternetAddress(email, email, "UTF-8");
                }
                message.setRecipients(MimeMessage.RecipientType.TO, addresses);
            }
            // Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
            message.setSubject(emailOption.getEmailTitle(), "UTF-8");
            //  Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
            message.setContent(emailOption.getContent(), "text/html;charset=UTF-8");
            // 设置发件时间
            message.setSentDate(new Date());
            // 保存设置
            message.saveChanges();
            //  根据 Session 获取邮件传输对象
            Transport transport = session.getTransport();
            transport.connect(emailOption.getEmailAccount(), emailOption.getEmailPassword());
            // 发送邮件, 发到所有的收件地址, message.getAllRecipients()获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(message, message.getAllRecipients());
            // 关闭传输连接
            transport.close();
            return true;
        } catch (Exception e) {
            log.error("发送邮件失败,系统错误！", e);
            return false;
        }
    }
}