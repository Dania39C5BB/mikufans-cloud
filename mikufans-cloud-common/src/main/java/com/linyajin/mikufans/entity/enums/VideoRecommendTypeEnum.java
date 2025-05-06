package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum VideoRecommendTypeEnum {
    NO_RECOMMEND(0, "不推荐"),
    RECOMMEND(1, "已推荐");

    private Integer type;
    private String desc;

    VideoRecommendTypeEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }
    public static VideoRecommendTypeEnum getByType(Integer status) {
        for (VideoRecommendTypeEnum item : VideoRecommendTypeEnum.values()) {
            if (item.type.equals(status)) {
                return item;
            }
        }
        return null;
    }
}
