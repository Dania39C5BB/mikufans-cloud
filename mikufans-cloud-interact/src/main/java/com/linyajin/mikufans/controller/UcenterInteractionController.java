package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoDanmu;
import com.linyajin.mikufans.entity.query.VideoCommentQuery;
import com.linyajin.mikufans.entity.query.VideoDanmuQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.VideoCommentService;
import com.linyajin.mikufans.service.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ucenter")
public class UcenterInteractionController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;


    /**
     * 查询视频评论(个人创作中心)
     * @param pageNo 页码
     * @param videoId 视频id
     * @return ResponseVO
     */
    @GetMapping("/loadComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadComment(Integer pageNo, String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        videoCommentQuery.setOrderBy("post_time desc");
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryVideoInfo(true);
        PaginationResultVO<VideoComment> commentPaginationResultVO = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(commentPaginationResultVO);
    }


    /**
     * 删除评论 (个人创作中心)
     * @param commentId 评论id
     * @return ResponseVO
     */
    @DeleteMapping("/deleteComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteComment(@NotNull  Integer commentId) {
        videoCommentService.delComment(commentId, getTokenUserInfoDto().getUserId());
        return getSuccessResponseVO(null);
    }


    /**
     * 查询视频弹幕(个人创作中心)
     * @param pageNo 页码
     * @param videoId 视频id
     * @return ResponseVO
     */
    @GetMapping("/loadDanMu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadDanMu(Integer pageNo,String videoId) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setQueryVideoInfo(true);
        videoDanmuQuery.setVideoUserId(getTokenUserInfoDto().getUserId());
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setOrderBy("post_time desc");
        PaginationResultVO<VideoDanmu> videoDanmuPaginationResultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(videoDanmuPaginationResultVO);
    }


    /**
     * 删除弹幕 (个人创作中心)
     * @param danMuId 弹幕id
     * @return ResponseVO
     */
    @DeleteMapping("/deleteDanMu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteDanMu(@NotNull  Integer danMuId) {
        videoDanmuService.deleteDanMu(danMuId, getTokenUserInfoDto().getUserId());
        return getSuccessResponseVO(null);
    }


}
