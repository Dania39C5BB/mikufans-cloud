package com.linyajin.mikufans.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostDanMuDto {
    @NotEmpty
    private String videoId;
    @NotEmpty
    private String fileId;
    @NotEmpty
    @Size(max=200)
    private String text;
    /**
     * 0: 滚动弹幕
     * 1: 顶部弹幕
     * 2: 底部弹幕
     */
    @NotNull
    private Integer mode;
    /**
     * 颜色：#FFFFFF
     */
    @NotEmpty
    private String color;
    /**
     * 时间：单位为秒
     */
    @NotNull
    private Integer time;
}
