package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum UserActionTypeEnum {
    COMMENT_LIKE(0,"like_count", "评论喜欢点赞"),
    COMMENT_HATE(1,"hate_count", "评论讨厌"),
    VIDEO_LIKE(2,"like_count", "视频点赞"),
    VIDEO_COLLECT(3,"collect_count", "视频收藏"),
    VIDEO_COIN(4,"coin_count", "视频投币"),
    VIDEO_COMMENT(5,"comment_count", "视频评论数"),
    VIDEO_DANMU(6,"danmu_count", "视频弹幕数"),
    VIDEO_PLAY(7,"play_count", "视频播放数");
    private final Integer type;
    private final String fileId;
    private final String desc;

    UserActionTypeEnum(Integer type, String fileId , String desc) {
        this.type = type;
        this.desc = desc;
        this.fileId = fileId;
    }
    public static UserActionTypeEnum getByType(int type) {
        for (UserActionTypeEnum item : UserActionTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }
}
