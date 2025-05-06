package com.linyajin.mikufans.entity.vo;


import com.linyajin.mikufans.entity.po.VideoInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoResultVO {
    private VideoInfo videoInfo;
    private List userActionList;
}
