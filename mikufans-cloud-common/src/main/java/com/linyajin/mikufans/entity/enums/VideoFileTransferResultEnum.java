package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum VideoFileTransferResultEnum {
    TRANSFER(0 , "转码中"),
    FAIL(2, "转码失败"),
    SUCCESS(1 , "转码成功");


    private Integer status;
    private String desc;
    VideoFileTransferResultEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
