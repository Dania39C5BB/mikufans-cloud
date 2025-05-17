package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum StatisticsTypeEnum {
    PLAY(0, "播放量"),
    FANS(1, "粉丝"),
    LIKE(2, "点赞"),
    COLLECTION(3, "收藏"),
    COIN(4, "投币"),
    COMMENT(5, "评论"),
    DANMU(6, "弹幕");

    private Integer type;
    private String desc;
    StatisticsTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static StatisticsTypeEnum getByType(Integer type) {
        for (StatisticsTypeEnum statisticsEnum : StatisticsTypeEnum.values()) {
            if (statisticsEnum.getType().equals(type)) {
                return statisticsEnum;
            }
        }
        return null;
    }

}
