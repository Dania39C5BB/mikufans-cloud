package com.linyajin.mikufans.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class VideoInfoEsDto {
    private String videoId;
    private String videoName;
    private String videoCover;
    private String userId;
    private String tags;
    private Integer playCount;
    private Integer danmuCount;
    private Integer collectCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
