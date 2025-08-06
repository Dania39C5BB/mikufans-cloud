package com.linyajin.mikufans.api.consumer;


import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.entity.po.VideoInfoFile;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mikufans-cloud-web")
public interface VideoInfoClient {

    @GetMapping("/innerApi/video/getVideoInfoFileByFileId")
    VideoInfoFile getVideoInfoFileByFileId(@RequestParam("fileId") String fileId);


    @PostMapping("/innerApi/video/transferVideoInfoFile")
    void transferVideoInfoFile(@RequestParam("videoId") String videoId,
                               @RequestParam("uploadId") String uploadId,
                               @RequestParam("userId") String userId,
                               @RequestBody VideoInfoFilePost uploadFilePost);
}
