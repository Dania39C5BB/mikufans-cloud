package com.linyajin.mikufans.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoVO implements Serializable {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     * 0：女 1：男 2：未知
     */
    private Integer sex;

    /**
     * 个人简介
     */
    private String personIntroduction;

    /**
     * 空间公告
     */
    private String noticeInfo;

    /**
     * 等级
     */
    private String grade;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 学校
     */
    private String school;

    /**
     * 粉丝数量
     */
    private Integer fansCount;

    /**
     * 关注数量
     */
    private Integer focusCount;

    /**
     * 喜欢的数量
     */
    private Integer likeCount;

    /**
     * 播放数量
     */
    private Integer playCount;

    /**
     * 是否关注
     */
    private Boolean haveFocus;

    /**
     * 主题
     */
    private Integer theme;


}
