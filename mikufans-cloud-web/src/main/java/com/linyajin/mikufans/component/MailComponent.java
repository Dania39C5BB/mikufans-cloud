package com.linyajin.mikufans.component;

import jakarta.annotation.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailComponent {

    /**
     * JavaMailSender是Spring Boot在MailSenderPropertiesConfiguration类中配置好的，
     * 该类在Mail自动配置类MailSenderAutoConfiguration中导入，因此这里注入JavaMailSender就可以使用了。
     */
    @Resource
    JavaMailSender javaMailSender;

    /**
     * sendSimpleMail方法的5个参数分别表示邮件发送者、收件人、抄送人、邮件主题以及邮件内容。
     *
     * @param from    发送者
     * @param to      收件人
     * @param cc      抄送人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleMail(String from, String to, String cc, String subject, String content) {
        // 简单邮件可以直接构建一个SimpleMailMessage对象进行配置，配置完成后，通过JavaMailSender将邮件发送出去。
        SimpleMailMessage simpMsg = new SimpleMailMessage();
        simpMsg.setFrom(from);
        simpMsg.setTo(to);
//        simpMsg.setCc(cc);
        simpMsg.setSubject(subject);
        simpMsg.setText(content);
        javaMailSender.send(simpMsg);
    }
}
