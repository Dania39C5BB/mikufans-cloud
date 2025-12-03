package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.po.VideoPlayHistory;
import com.linyajin.mikufans.entity.query.VideoPlayHistoryQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/history")
public class VideoPlayHistoryController extends ABaseController {


    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    /**
     * 加载历史视频列表
     * @param pageNo 页码
     * @return  ResponseVO
     */
    @GetMapping("/loadHistoryVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadHistory(Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryQuery.setPageNo(pageNo);
        videoPlayHistoryQuery.setOrderBy("last_update_time desc");
        videoPlayHistoryQuery.setQueryVideoDetail(true);
        PaginationResultVO<VideoPlayHistory> resultVO = videoPlayHistoryService.findListByPage(videoPlayHistoryQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 清空历史记录的视频
     * @return ResponseVO
     */
    @DeleteMapping("/cleanHistoryVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cleanHistoryVideo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryService.deleteByParam(videoPlayHistoryQuery);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除单个历史记录的视频
     * @param videoId  视频id
     * @return ResponseVO
     */
    @DeleteMapping("/deleteHistoryVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteHistoryVideo(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(tokenUserInfoDto.getUserId() , videoId);
        return getSuccessResponseVO(null);
    }
}
