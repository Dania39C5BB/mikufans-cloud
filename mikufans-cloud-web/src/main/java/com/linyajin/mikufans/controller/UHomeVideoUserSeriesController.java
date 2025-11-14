package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.po.UserVideoSeries;
import com.linyajin.mikufans.entity.po.UserVideoSeriesVideo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.UserVideoSeriesQuery;
import com.linyajin.mikufans.entity.query.UserVideoSeriesVideoQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.entity.vo.VideoSeriesVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.service.UserVideoSeriesService;
import com.linyajin.mikufans.service.UserVideoSeriesVideoService;
import com.linyajin.mikufans.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频合集
 */
@RestController
@Validated
@RequestMapping("/uHome/series")
public class UHomeVideoUserSeriesController extends ABaseController {

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoSeriesVideoService userVideoSeriesVideoService;


    /**
     * 获取视频合集列表的第一个视频(封面作用)
     * @param userId 用户id
     * @return ResponseVO
     */
    @GetMapping("/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId) {
        List<UserVideoSeries> userVideoSeriesCover = userVideoSeriesService.getUserVideoSeriesCover(userId);
        return getSuccessResponseVO(userVideoSeriesCover);
    }

    /**
     * 创建视频合集并添加视频到合集
     * @param seriesId 合集id
     * @param seriesName 合集名称
     * @param description 合集描述
     * @param videoIds 视频id集合，逗号分隔
     * @return ResponseVO
     */
    @PostMapping("/saveVideoSeries")
    public ResponseVO saveVideoSeries(Integer seriesId,
                                    @NotEmpty String seriesName,
                                    @Size(max = 200) String description,
                                    String videoIds){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserVideoSeries userVideoSeries = new UserVideoSeries();

        userVideoSeries.setSeriesId(seriesId);
        userVideoSeries.setSeriesName(seriesName);
        userVideoSeries.setSeriesDescription(description);
        userVideoSeries.setUserId(tokenUserInfoDto.getUserId());

        userVideoSeriesService.saveVideoSeries(userVideoSeries,videoIds);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取我的视频列表
     * @param seriesId 合集id
     * @return ResponseVO
     */
    @GetMapping("/loadAllVideo")
    public ResponseVO loadAllVideo(Integer seriesId){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        //在视频集合中添加视频的时候需要判断当前集合中有没有存在该视频，如果存在则需要过滤掉
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (seriesId != null) {
            //查询该用户下的所有视频 然后过滤掉已经存在当前合集下的视频
            //获取集合中的视频
            UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
            userVideoSeriesVideoQuery.setSeriesId(seriesId);
            userVideoSeriesVideoQuery.setUserId(tokenUserInfoDto.getUserId());
            List<UserVideoSeriesVideo> seriesInVideos = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);

            //获取集合中的所有视频id
            List<String> videoIds = seriesInVideos.stream().map(item -> item.getVideoId()).collect(Collectors.toList());
            //过滤掉已经存在当前合集下的视频
            videoInfoQuery.setExcludeVideoIds(videoIds.toArray(new String[videoIds.size()]));
        }
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * 获取视频合集详情
     * @param seriesId 合集id
     * @return ResponseVO
     */
    @GetMapping("/loadVideoSeriesDetail")
    public ResponseVO loadVideoSeriesDetail(@NotNull Integer seriesId){
        UserVideoSeries dbSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if (dbSeries == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        userVideoSeriesVideoQuery.setUserId(tokenUserInfoDto.getUserId());
        userVideoSeriesVideoQuery.setSeriesId(seriesId);
        userVideoSeriesVideoQuery.setOrderBy("sort asc");
        userVideoSeriesVideoQuery.setQueryVideoInfo(true);
        List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);
        VideoSeriesVO videoSeriesVO = new VideoSeriesVO(dbSeries,seriesVideoList);

        return  getSuccessResponseVO(videoSeriesVO);
    }


    /**
     *
     * @param seriesId 合集id
     * @param videoIds 视频id集合，逗号分隔
     * @return ResponseVO
     */
    @PostMapping("/saveSeriesVideo")
    public ResponseVO saveSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoIds){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.saveVideoInSeries(seriesId,tokenUserInfoDto.getUserId(),videoIds);
        return getSuccessResponseVO(null);
    }


    /**
     * 删除集合中的视频
     * @param seriesId 合集id
     * @param videoId 视频id集合，逗号分隔
     * @return ResponseVO
     */
    @DeleteMapping("/deleteSeriesInVideo")
    public ResponseVO deleteSeriesInVideo(@NotNull Integer seriesId, @NotEmpty String videoId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.deleteSeriesInVideo(seriesId,tokenUserInfoDto.getUserId(),videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * 更新视频合集的排序
     * @param seriesIds 合集id
     * @return ResponseVO
     */
    @PostMapping("/updateSeriesSort")
    public ResponseVO updateSeriesSort(@NotEmpty String seriesIds){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.changeVideoSeriesSort(seriesIds,tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }


    //获取个人主页的视频合集列表
    @GetMapping("/loadUserVideoSeries")
    public ResponseVO loadUserVideoSeries(@NotEmpty String userId){
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setOrderBy("sort asc");
        List<UserVideoSeries> videoSeriesList = userVideoSeriesService.findUserHomeVideoSeries(userVideoSeriesQuery);
        return getSuccessResponseVO(videoSeriesList);
    }


}
