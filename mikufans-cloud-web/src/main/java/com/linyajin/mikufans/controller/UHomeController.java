package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.UpdateUserInfoDto;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.VideoOrderTypeEnum;
import com.linyajin.mikufans.entity.po.UserFocus;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.UserFocusQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.entity.vo.UserInfoVO;
import com.linyajin.mikufans.service.UserFocusService;
import com.linyajin.mikufans.service.UserInfoService;
import com.linyajin.mikufans.service.VideoInfoService;
import com.linyajin.mikufans.utils.CopyTools;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/uHome")
public class UHomeController extends ABaseController {

    @Resource
    private UserFocusService userFocusService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserInfoService userInfoService;


    /**
     * 获取用户信息
     * @param userId 用户id
     * @return userInfoVO 用户信息vo
     */
    @GetMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String currentUserId = tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId();
        UserInfo userDetailsInfo = userInfoService.getUserDetailsInfo(currentUserId, userId);

        UserInfoVO userInfoVO = CopyTools.copy(userDetailsInfo, UserInfoVO.class);

        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 更新用户信息
     * @param updateUserInfoDto 更新用户信息dto
     * @return ResponseVO 返回结果vo
     */
    @PostMapping("/updateUserInfo")
    public ResponseVO updateUserInfo(@RequestBody @Valid UpdateUserInfoDto updateUserInfoDto) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setSex(updateUserInfoDto.getSex());
        userInfo.setBirthday(updateUserInfoDto.getBirthday());
        userInfo.setSchool(updateUserInfoDto.getSchool());
        userInfo.setNoticeInfo(updateUserInfoDto.getNoticeInfo());
        userInfo.setPersonIntroduction(updateUserInfoDto.getPersonIntroduction());
        userInfo.setAvatar(updateUserInfoDto.getAvatar());
        userInfo.setNickName(updateUserInfoDto.getNickName());
        userInfoService.updateUserInfo(userInfo ,tokenUserInfoDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 保存主题
     * @param theme 主题Id
     * @return ResponseVO 返回结果vo
     */
    @PutMapping("/saveTheme")
    public ResponseVO saveTheme(@NotNull Integer theme) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfoService.updateUserInfoByUserId(userInfo,tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }


    /**
     * 关注用户
     * @param focusUserId 要关注用户的id
     * @return ResponseVO 返回结果vo
     */
    @PostMapping("/focusUser")
    public ResponseVO focusUser(@NotEmpty String focusUserId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userFocusService.focusUser(tokenUserInfoDto.getUserId(), focusUserId);
        return getSuccessResponseVO(null);
    }


    /**
     * 取消关注用户
     * @param focusUserId 要取消关注用户的id
     * @return ResponseVO 返回结果vo
     */
    @PostMapping("/cancelFocusUser")
    public ResponseVO cancelFocusUser(@NotEmpty String focusUserId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userFocusService.cancelFocusUser(tokenUserInfoDto.getUserId(), focusUserId);
        return getSuccessResponseVO(null);
    }


    //查询粉丝列表
    @GetMapping("/loadFansList")
    public ResponseVO loadFansList(Integer  pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setFocusUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ONE);
        PaginationResultVO<UserFocus> resultVO = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(resultVO);
    }

    //查询关注列表
    @GetMapping("/loadFocusList")
    public ResponseVO loadFocusList(Integer  pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ZERO);
        PaginationResultVO<UserFocus> resultVO = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 查询主页视频列表
     * @param userId 用户id
     * @param pageNo 页码
     * @param type 视频类型
     * @param orderType 排序类型
     * @param VideoName 视频名称
     * @return ResponseVO 返回结果vo
     */
    @GetMapping("/loadVideoList")
    public ResponseVO loadUserAction(@NotEmpty String userId ,
                                     Integer  pageNo , Integer type ,
                                     Integer orderType,String VideoName) {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        //如果type不等于null代表是主页的默认10个展示视频 否则是自己投稿的分页列表
        if (type != null) {
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());
        }
        //获取排序类型
        VideoOrderTypeEnum orderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        //如果排序类型为空，则默认为创建时间排序
        if (orderTypeEnum == null) {
            orderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        videoInfoQuery.setOrderBy(orderTypeEnum.getFileId() + " desc");
        videoInfoQuery.setVideoNameFuzzy(VideoName);

        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);

        return getSuccessResponseVO(resultVO);
    }

}
