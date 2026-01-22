package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.entity.po.EmailOption;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.utils.EmailSender;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/email")
public class EmailController extends ABaseController {
    @Resource
    private EmailSender emailSender;

    @PostMapping("/send")
    public ResponseVO sendEmail(@RequestBody @Valid EmailOption emailOption){
        EmailOption emailInfo = new EmailOption();
        emailInfo.setEmailAccount(emailOption.getEmailAccount());
        emailInfo.setEmailPassword(emailOption.getEmailPassword());
        emailInfo.setContent(emailOption.getContent());
        emailInfo.setSendUserName(emailOption.getSendUserName());
        emailInfo.setReceiveEmailAccount(emailOption.getReceiveEmailAccount());
        emailInfo.setEmailTitle(emailOption.getEmailTitle());

        Set<String> set = new HashSet<>();
        set.add("3247319022@qq.com");
        emailSender.sendEmail(emailOption,set);

        return getSuccessResponseVO(null);
    }
}
