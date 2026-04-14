package com.linyajin.mikufans.api.provider;


import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoDanmu;
import com.linyajin.mikufans.entity.query.UserActionQuery;
import com.linyajin.mikufans.entity.query.VideoCommentQuery;
import com.linyajin.mikufans.entity.query.VideoDanmuQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.UserActionService;
import com.linyajin.mikufans.service.VideoCommentService;
import com.linyajin.mikufans.service.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/innerApi/interact")
@Validated
@Slf4j
public class InteractApi {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private UserActionService userActionService;

    /**
     * 获取评论
     * @param pageNo 页码
     * @param videoNameFuzzy 模糊查询视频名
     * @return ResponseVO
     */
    @GetMapping("/admin/loadComment")
    public PaginationResultVO<VideoComment> loadComment(Integer pageNo, String videoNameFuzzy,Integer pageSize) {

        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setQueryVideoInfo(true);
        videoCommentQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoCommentQuery.setOrderBy("post_time desc");
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setPageSize(pageSize);
        PaginationResultVO<VideoComment> resultVO = videoCommentService.findListByPage(videoCommentQuery);
        return resultVO;
    }


    /**
     * 删除评论
     * @param commentId 评论id
     */
    @DeleteMapping("/admin/delComment")
    public void delComment(@NotNull Integer commentId) {
        videoCommentService.delComment(commentId , null);
    }

    /**
     * 获取弹幕
     * @param pageNo 页码
     * @param videoNameFuzzy 模糊查询视频名
     * @return ResponseVO
     */
    @GetMapping("/admin/loadDanMu")
    public PaginationResultVO<VideoDanmu> loadDanMu(Integer pageNo , String videoNameFuzzy) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setOrderBy("post_time desc");
        videoDanmuQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoDanmuQuery.setQueryVideoInfo(true);
        PaginationResultVO<VideoDanmu> resultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return resultVO;
    }

    /**
     * 删除弹幕
     * @param danMuId 弹幕id
     */
    @DeleteMapping("/admin/delDanMu")
    public void delDanMu(@NotNull Integer danMuId) {
        videoDanmuService.deleteDanMu(danMuId , null);
    }

    //删除评论
    @DeleteMapping("/delCommentByVideoId")
    public void delCommentByVideoId(@NotEmpty String videoId) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentService.deleteByParam(videoCommentQuery);
    }

    //删除弹幕
    @DeleteMapping("/delDanMuByVideoId")
    public void delDanMuByVideoId(@NotEmpty String videoId) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuService.deleteByParam(videoDanmuQuery);
    }

    @PostMapping("/userAction/getUserActionList")
    public List<UserAction> getUserActionList(@RequestBody UserActionQuery userActionQuery) {
        return userActionService.findListByParam(userActionQuery);
    }

}
