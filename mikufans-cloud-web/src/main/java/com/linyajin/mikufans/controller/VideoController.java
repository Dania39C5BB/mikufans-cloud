package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.api.consumer.InteractClient;
import com.linyajin.mikufans.component.EsSearchComponent;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.*;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.po.VideoInfoFile;
import com.linyajin.mikufans.entity.query.UserActionQuery;
import com.linyajin.mikufans.entity.query.VideoInfoFileQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.entity.vo.VideoResultVO;
import com.linyajin.mikufans.exception.BusinessException;
//import com.linyajin.mikufans.service.UserActionService;
import com.linyajin.mikufans.service.VideoInfoFileService;
import com.linyajin.mikufans.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/video")
public class VideoController extends ABaseController {

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private InteractClient interactClient;


    //获取首页推荐视频
    @GetMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo() {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        List<VideoInfo> recommendList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendList);
    }


    //获取首页视频列表
    @PostMapping("/loadVideoList")
    public ResponseVO loadVideoList( Integer categoryId,Integer pCategoryId , Integer pageNo , Integer pageSize) {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setPageSize(pageSize);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());

        PaginationResultVO<VideoInfo> videoInfoList = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    //获取视频详情信息
    @GetMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //TODO 获取用户行为，点赞，投币，收藏等信息
        List<UserAction> userActionList = new ArrayList<>();
        //判断是否登录
        TokenUserInfoDto userInfoDto = getTokenUserInfoDto();
        if (userInfoDto != null) {
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            //获取当前是哪个用户的操作
            userActionQuery.setUserId(userInfoDto.getUserId());
            //获取点赞，投币，收藏的数量
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(),UserActionTypeEnum.VIDEO_COLLECT.getType(),
                UserActionTypeEnum.VIDEO_COIN.getType()});
            //userActionList = userActionService.findListByParam(userActionQuery);
            //TODO 调用行为模块
            userActionList = interactClient.getUserActionList(userActionQuery);
        }
        VideoResultVO videoResultVO = new VideoResultVO(videoInfo , userActionList);

        return getSuccessResponseVO(videoResultVO);
    }

    //获取视频分P信息
    @GetMapping("/getPVideoInfo")
    public ResponseVO getPVideoInfo(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> videoPInfo = videoInfoFileService.findListByParam(videoInfoFileQuery);
        return getSuccessResponseVO(videoPInfo);
    }

    //搜索视频
    @GetMapping("/search")
    public ResponseVO searchVideo(@NotEmpty String keyword ,Integer orderType , Integer pageNo) {
        //TODO 搜索视频记录热词
        redisComponent.addKeyWordCount(keyword);
        PaginationResultVO<VideoInfo> search = esSearchComponent.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize());
        return getSuccessResponseVO(search);
    }

    //从es中获取推荐视频
    @GetMapping("/getRecommendVideo")
    public ResponseVO getRecommendVideo(@NotEmpty String keyword ,@NotEmpty String videoId) {
        PaginationResultVO<VideoInfo> recommendVideo = esSearchComponent.search(false, keyword, SearchOrderTypeEnum.VIDEO_PLAY.getType(), 1, PageSize.SIZE10.getSize());
        //把推荐的视频当中排除掉当前的这个视频
        //排除掉当前视频，只保留其他推荐视频
        List<VideoInfo> resultVO = recommendVideo.getList().stream().filter(item -> !item.getVideoId().equals(videoId)).collect(Collectors.toList());
        return getSuccessResponseVO(resultVO);
    }

    //获取当天的搜索热词
    @GetMapping("/getHotKeyWord")
    public ResponseVO getHotKeyWord() {
        List<String> dailyTopKeywords = redisComponent.getDailyTopKeywords(LocalDate.now(), 10);
        return getSuccessResponseVO(dailyTopKeywords);
    }

    //获取24小时内的热门视频
    @GetMapping("/getHotVideoList")
    public ResponseVO getHotKeyWordByDate(Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setOrderBy("play_count desc");
        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }
}
