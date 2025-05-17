package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum SearchOrderTypeEnum {
    VIDEO_PLAY(0,"playCount" , "视频播放数"),
    VIDEO_TIME(1,"createTime" , "视频时间"),
    VIDEO_DANMU(2,"danmuCount" , "弹幕数"),
    VIDEO_COLLECT(3,"collectCount" , "收藏数");

    private Integer type;
    private String fileId;
    private String desc;

    SearchOrderTypeEnum(Integer type, String fileId, String desc) {
        this.type = type;
        this.fileId = fileId;
        this.desc = desc;
    }

    public static SearchOrderTypeEnum getByType(Integer type){
        for (SearchOrderTypeEnum value : SearchOrderTypeEnum.values()) {
            if(value.getType().equals(type)){
                return value;
            }
        }
        return null;
    }
}
