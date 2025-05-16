package com.linyajin.mikufans.entity.vo;

import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import lombok.Data;

import java.util.List;

@Data
public class VideoPostEditInfoVO {
    private VideoInfoPost videoInfoPost;
    //分P信息列表
    private List<VideoInfoFilePost> videoInfoFileList;
}
