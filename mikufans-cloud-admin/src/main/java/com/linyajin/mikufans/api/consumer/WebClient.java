package com.linyajin.mikufans.api.consumer;


import com.linyajin.mikufans.entity.po.StatisticsInfo;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.VideoInfoPostQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.fallback.WebClientFallbackFactory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mikufans-cloud-web" , fallbackFactory = WebClientFallbackFactory.class)
public interface WebClient {

    //获取昨天和全部的统计信息(管理端)
    @GetMapping("/innerApi/statistics/admin/getActualTimeStatisticsInfo")
    public Map getActualTimeStatisticsInfo();

    //获取近7天的统计信息
    @GetMapping("/innerApi/statistics/admin/getWeekStatisticsInfo")
    public List<StatisticsInfo> getWeekStatisticsInfo(@RequestParam("dateType") Integer dateType);

    //管理端查询发布之后的稿件
    @GetMapping("/innerApi/video/admin/loadVideoList")
    public PaginationResultVO<VideoInfoPost> loadVideoList(@RequestBody VideoInfoPostQuery videoInfoPostQuery);

    //审核视频
    @GetMapping("/innerApi/video/admin/auditVideo")
    public void auditVideo(@RequestParam("videoId") String videoId, @RequestParam("status") Integer status ,@RequestParam("reason") String reason);

    //是否推荐视频
    @GetMapping("/innerApi/video/admin/recommendVideo")
    public void recommendVideo(@RequestParam("videoId") String videoId);

    //删除视频
    @DeleteMapping("/innerApi/video/admin/deleteVideo")
    public void deleteVideo(@RequestParam("videoId") String videoId);

    //获取视频分P信息
    @GetMapping("/innerApi/video/admin/loadPVideoList")
    public List<VideoInfoFilePost> loadPVideoList(@RequestParam("videoId") String videoId);


    //获取视频数量
    @GetMapping("/innerApi/video/admin/getVideoCount")
    public Integer getVideoCount(@RequestBody VideoInfoQuery videoInfoQuery);
}
