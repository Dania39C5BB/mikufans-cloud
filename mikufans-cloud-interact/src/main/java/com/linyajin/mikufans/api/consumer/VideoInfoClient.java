package com.linyajin.mikufans.api.consumer;


import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mikufans-cloud-web")
public interface VideoInfoClient {

    //更新用户的硬币数量
    @GetMapping("/innerApi/user/updateCoinCount")
    Integer updateCoinCount(@RequestParam("userId") String userId,@RequestParam("count") Integer count);

    //获取用户信息
    @GetMapping("/innerApi/user/selectByUserId")
    UserInfo selectByUserId(@RequestParam("userId") String userId);


    //根据视频id查询视频信息
    @GetMapping("/innerApi/video/getVideoSelectByVideoId")
    VideoInfo getVideoSelectByVideoId(@RequestParam("videoId") String videoId);

    //更新视频点赞操作收藏的数量
    @PutMapping("/innerApi/video/updateCountInfo")
    void updateCountInfo(@RequestParam("videoId") String videoId,@RequestParam("fileId") String fileId,@RequestParam("changeCount") Integer changeCount);

    //更新es的收藏数量
    @PutMapping("/innerApi/video/updateDocCount")
    VideoInfo updateDocCount(@RequestParam("videoId") String videoId , @RequestParam SearchOrderTypeEnum searchOrderTypeEnum ,@RequestParam("changeCount") Integer changeCount);

    //获取发布表中的视频信息
    @GetMapping("/innerApi/video/getVideoPostSelectByVideoId")
    VideoInfoPost getVideoPostSelectByVideoId(@RequestParam("videoId") String videoId);
}
