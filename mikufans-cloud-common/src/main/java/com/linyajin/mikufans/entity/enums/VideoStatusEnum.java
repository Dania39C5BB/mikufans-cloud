package com.linyajin.mikufans.entity.enums;

import lombok.Getter;

@Getter
public enum VideoStatusEnum {
    STATUS0(0, "转码中"),
    STATUS1(1, "转码失败"),
    STATUS2(2, "待审核"),
    STATUS3(3, "审核成功"),
    STATUS4(4, "审核不成功");

    private Integer status;
    private String desc;
    VideoStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static VideoStatusEnum getByStatus(Integer status) {
        for (VideoStatusEnum videoStatus : VideoStatusEnum.values()) {
            if (videoStatus.getStatus().equals(status)) {
                return videoStatus;
            }
        }
        return null;
    }

}
