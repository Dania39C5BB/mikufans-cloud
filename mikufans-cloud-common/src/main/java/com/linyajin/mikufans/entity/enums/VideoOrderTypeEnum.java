package com.linyajin.mikufans.entity.enums;


import lombok.Getter;

@Getter
public enum VideoOrderTypeEnum {
    CREATE_TIME(0,"create_time" , "创建时间"),
    PLAY_COUNT(1,"play_count" , "最多播放"),
    COLLECT_COUNT(2,"collect_count" , "最多收藏");

    private Integer type;
    private String fileId;
    private String desc;

    VideoOrderTypeEnum(Integer type, String fileId, String desc) {
        this.type = type;
        this.fileId = fileId;
        this.desc = desc;
    }

    public static VideoOrderTypeEnum getByType(Integer type) {
        for (VideoOrderTypeEnum item : VideoOrderTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

}
