package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum VideoFileUpdateTypeEnum {
    NO_UPDATE(0 , "未更新"),
    UPDATE(1 , "有更新");

    private Integer status;
    private String desc;

    VideoFileUpdateTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
