package com.linyajin.mikufans.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class UserMessageExtendDto {
    private String messageContent;
    private String messageContentReply;
    private Integer auditStatus;
}
