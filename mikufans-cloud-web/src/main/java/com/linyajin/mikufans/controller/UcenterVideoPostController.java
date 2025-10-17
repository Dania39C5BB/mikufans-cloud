package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.dto.PostVideoDto;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.VideoStatusEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.VideoInfoFilePostQuery;
import com.linyajin.mikufans.entity.query.VideoInfoPostQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.entity.vo.VideoPostEditInfoVO;
import com.linyajin.mikufans.entity.vo.VideoStatusCountInfoVo;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.service.VideoInfoFilePostService;
import com.linyajin.mikufans.service.VideoInfoPostService;
import com.linyajin.mikufans.service.VideoInfoService;
import com.linyajin.mikufans.utils.JsonUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ucenter")
public class UcenterVideoPostController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(UcenterVideoPostController.class);
    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;


    // 上传视频信息
    @PostMapping("/postVideo")
    public ResponseVO postVideo( @RequestBody @Valid PostVideoDto postVideoDto) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        List<VideoInfoFilePost> videoInfoFilePostList = JsonUtils.covertJsonArray2List(postVideoDto.getUploadFileList(), VideoInfoFilePost.class);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(postVideoDto.getVideoId());
        videoInfoPost.setVideoCover(postVideoDto.getVideoCover());
        videoInfoPost.setVideoName(postVideoDto.getVideoName());
        videoInfoPost.setPCategoryId(postVideoDto.getPCategoryId());
        videoInfoPost.setCategoryId(postVideoDto.getCategoryId());
        videoInfoPost.setPostType(postVideoDto.getPostType());
        videoInfoPost.setTags(postVideoDto.getTags());
        videoInfoPost.setIntroduction(postVideoDto.getIntroduction());
        videoInfoPost.setInteraction(postVideoDto.getInteraction());

        videoInfoPost.setUserId(tokenUserInfoDto.getUserId());

        videoInfoPostService.saveVideoInfoPost(videoInfoPost, videoInfoFilePostList);
        return getSuccessResponseVO(null);
    }

    //查询发布之后的稿件
    @GetMapping("/loadVideoList")
    public ResponseVO loadVideoList(Integer status , Integer pageNo , Integer pageSize , String videoNameFuzzy){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        videoInfoPostQuery.setPageNo(pageNo);
        videoInfoPostQuery.setPageSize(pageSize);
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        log.info("用户ID:{}" , tokenUserInfoDto.getUserId());
        if (status != null) {
            //进行审核中的稿件
            if (status == -1) {
                //排除掉审核中和已审核失败的稿件
                videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
            } else{
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        //查询数量信息
        videoInfoPostQuery.setQueryCountInfo(true);
        //TODO 这里分页可能有问题 待测试
        PaginationResultVO<VideoInfoPost> resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVO(resultVO);
    }

    //获取状态视频的数量
    @GetMapping("/getVideoStatusCount")
    public ResponseVO getVideoStatusCount(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        //审核成功的视频数量
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        //审核失败的视频数量
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        //除了审核成功和审核失败的视频数量
        videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()});
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        VideoStatusCountInfoVo videoStatusCountInfoVo = new VideoStatusCountInfoVo();
        videoStatusCountInfoVo.setAuditFailCount(auditFailCount);
        videoStatusCountInfoVo.setAuditPassCount(auditPassCount);
        videoStatusCountInfoVo.setInProgress(inProgress);
        return getSuccessResponseVO(videoStatusCountInfoVo);
    }


    /**
     * 根据视频ID查询视频详情
     * @param videoId 视频ID
     * @return ResponseVO
     */
    @GetMapping("/getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotNull String videoId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPost videoInfoPostByVideoId = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        //如果数据库的数据为空或者 视频不属于当前用户 则抛出异常信息
        if (videoInfoPostByVideoId == null || !videoInfoPostByVideoId.getUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询视频分P文件信息
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);

        VideoPostEditInfoVO videoPostEditInfoVO = new VideoPostEditInfoVO();
        videoPostEditInfoVO.setVideoInfoPost(videoInfoPostByVideoId);
        videoPostEditInfoVO.setVideoInfoFileList(videoInfoFilePostList);

        return getSuccessResponseVO(videoPostEditInfoVO);
    }

    /**
     * 保存视频互动信息
     * @param videoId  视频ID
     * @param interaction  互动信息
     * @return ResponseVO
     */
    @PostMapping("/saveVideoInteraction")
    public ResponseVO saveVideoInteraction(@NotNull String videoId , String interaction){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoInfoService.saveVideoInteraction(videoId , tokenUserInfoDto.getUserId(), interaction);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除视频稿件信息
     * @param videoId 视频ID
     * @return ResponseVO
     */
    @DeleteMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotNull String videoId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoInfoService.deleteVideo(videoId , tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
