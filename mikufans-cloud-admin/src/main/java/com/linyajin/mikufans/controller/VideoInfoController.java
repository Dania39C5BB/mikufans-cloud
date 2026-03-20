package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.api.consumer.WebClient;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.VideoInfoFilePostQuery;
import com.linyajin.mikufans.entity.query.VideoInfoPostQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video")
@Validated
public class VideoInfoController extends ABaseController {

    @Resource
    private WebClient webClient;

    //管理端查询发布之后的稿件
    @PostMapping("/loadVideoList")
    public ResponseVO loadVideoList(@RequestBody VideoInfoPostQuery videoInfoPostQuery){
        return getSuccessResponseVO(webClient.loadVideoList(videoInfoPostQuery));
    }

    //审核视频
    @PostMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public ResponseVO loadVideoList(@NotEmpty String videoId, @NotNull Integer status ,String reason){
        webClient.auditVideo(videoId,status ,reason);
        return getSuccessResponseVO(null);
    }

    /**
     * 是否推荐视频
     * @param videoId 视频id
     * @return ResponseVO
     */
    @PostMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId){
        webClient.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }


    /**
     * TODO:这里有一个争议点 如果在管理员这边删除了之后 那么是否应该通知用户被删除了呢？(待做)
     * 删除视频
     * @param videoId
     * @return
     */
    @DeleteMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId){
        webClient.deleteVideo(videoId);
        return getSuccessResponseVO(null);
    }


    //获取视频分P信息
    @GetMapping("/loadPVideoList")
    public ResponseVO loadPVideoList(@NotEmpty String videoId) {
        return getSuccessResponseVO(webClient.loadPVideoList(videoId));
    }


}
