package com.linyajin.mikufans.api.provider;

import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.component.EsSearchComponent;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.po.VideoInfoFile;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.VideoInfoFilePostQuery;
import com.linyajin.mikufans.entity.query.VideoInfoPostQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.VideoInfoFilePostService;
import com.linyajin.mikufans.service.VideoInfoFileService;
import com.linyajin.mikufans.service.VideoInfoPostService;
import com.linyajin.mikufans.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/innerApi/video")
@Validated
public class VideoInfoApi {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @GetMapping("/getVideoInfoFileByFileId")
    public VideoInfoFile getVideoInfoFileByFileId(@NotEmpty String fileId) {
        return videoInfoFileService.getVideoInfoFileByFileId(fileId);
    }



    @GetMapping("/getVideoSelectByVideoId")
    public VideoInfo getVideoSelectByVideoId(@NotEmpty String videoId) {
        return videoInfoService.getVideoInfoByVideoId(videoId);
    }


    @PutMapping("/updateCountInfo")
    public void updateCountInfo(@NotEmpty String videoId, @NotEmpty String fileId, @NotNull  Integer changeCount){
        videoInfoService.updateCountInfo(videoId,fileId,changeCount);
    }

    @GetMapping("/getVideoPostSelectByVideoId")
    public VideoInfoPost getVideoPostSelectByVideoId(@NotEmpty String videoId) {
        return videoInfoPostService.getVideoInfoPostByVideoId(videoId);
    }

    @PutMapping("/updateDocCount")
    public void updateDocCount(@NotEmpty String videoId, @NotNull SearchOrderTypeEnum searchOrderTypeEnum, @NotNull  Integer changeCount){
        esSearchComponent.updateDocCount(videoId,searchOrderTypeEnum.getFileId(),changeCount);
    }


    @PostMapping("/admin/loadVideoList")
    public PaginationResultVO<VideoInfoPost> loadVideoList(@RequestBody VideoInfoPostQuery videoInfoPostQuery){
        videoInfoPostQuery.setOrderBy("v.last_update_time desc");
        videoInfoPostQuery.setQueryCountInfo(true);
        videoInfoPostQuery.setQueryUserInfo(true);
        PaginationResultVO<VideoInfoPost> resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return resultVO;
    }

    @GetMapping("/admin/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public void auditVideo(@NotEmpty String videoId, @NotNull Integer status ,String reason){
        videoInfoPostService.auditVideo(videoId,status,reason);
    }

    @PostMapping("/admin/recommendVideo")
    public void recommendVideo(@NotEmpty String videoId){
        videoInfoService.recommendVideo(videoId);
    }


    @DeleteMapping("/admin/deleteVideo")
    public void deleteVideo(@NotEmpty String videoId){
        videoInfoService.deleteVideo(videoId , null);
    }

    //获取视频分P信息
    @GetMapping("/admin/loadPVideoList")
    public List<VideoInfoFilePost> loadPVideoList(@NotEmpty String videoId) {
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> VideoInfoFilePost = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        return VideoInfoFilePost;
    }

    @PostMapping("/admin/getVideoCount")
    public Integer getVideoCount(@RequestBody VideoInfoQuery videoInfoQuery){
        return videoInfoService.findCountByParam(videoInfoQuery);
    }



    //转码视频文件任务队列 更新视频文件信息 /admin/transferVideoInfoFile
    @PostMapping("/transferVideoInfoFile")
    public void transferVideoInfoFile(@NotEmpty String videoId,
                                      @NotEmpty String uploadId,
                                      @NotEmpty String userId,
                                      @RequestBody VideoInfoFilePost uploadFilePost){


        videoInfoPostService.transferVideoInfoFileDb(videoId,uploadId,userId,uploadFilePost);
    }




}
