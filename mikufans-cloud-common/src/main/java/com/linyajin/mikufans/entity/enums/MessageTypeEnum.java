package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    SYS(0,"系统"),
    LIKE(1,"点赞"),
    COLLECTION(2,"收藏"),
    COMMENT(3,"评论");
    private final Integer type;
    private final String desc;

    MessageTypeEnum(Integer type,String desc){
        this.type = type;
        this.desc = desc;
    }

    public static MessageTypeEnum getByType(Integer type){
        for (MessageTypeEnum message : MessageTypeEnum.values()) {
            if(message.getType().equals(type)){
                return message;
            }
        }
        return null;
    }
}
