package com.linyajin.mikufans.entity.vo;

import lombok.Data;

@Data
public class VideoStatusCountInfoVo {
    //通过审核的视频数
    private Integer auditPassCount;
    //审核失败的视频数
    private Integer auditFailCount;
    //进行中的视频数
    private Integer inProgress;
}
