package com.linyajin.mikufans.entity.vo;

import com.linyajin.mikufans.entity.po.UserVideoSeries;
import com.linyajin.mikufans.entity.po.UserVideoSeriesVideo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoSeriesVO {

    private UserVideoSeries userVideoSeries;
    private List<UserVideoSeriesVideo> seriesVideoList;
}
