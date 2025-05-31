package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.PostDanMuDto;
import com.linyajin.mikufans.entity.po.VideoDanmu;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.VideoDanmuQuery;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;


/**
 *  Controller
 */
@RestController
@Validated
@RequestMapping("/danmu")
public class VideoDanmuController extends ABaseController{

	@Resource
	private VideoInfoClient videoInfoClient;

	@Resource
	private VideoDanmuService videoDanmuService;


	/**
	 * 发布弹幕
	 * @param postDanMuDto 发布弹幕的信息
	 * @return ResponseVO 返回结果
	 */
	@PostMapping("/postDanMu")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO loadDanMu(@RequestBody @Valid PostDanMuDto postDanMuDto){
		VideoDanmu videoDanmu = new VideoDanmu();
		videoDanmu.setVideoId(postDanMuDto.getVideoId());
		videoDanmu.setFileId(postDanMuDto.getFileId());
		videoDanmu.setText(postDanMuDto.getText());
		videoDanmu.setMode(postDanMuDto.getMode());
		videoDanmu.setColor(postDanMuDto.getColor());
		videoDanmu.setTime(postDanMuDto.getTime());
		videoDanmu.setUserId(getTokenUserInfoDto().getUserId());
		videoDanmu.setPostTime(new Date());
		videoDanmuService.saveVideoDanMu(videoDanmu);
		return getSuccessResponseVO(null);
	}

	@GetMapping("/loadDanMu")
	public ResponseVO loadDanMu(@NotEmpty String videoId ,@NotEmpty String fileId){
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoId);
		//查询该视频是否关闭弹幕或者评论
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
			return getSuccessResponseVO(new ArrayList<>());
		}
		VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
		videoDanmuQuery.setFileId(fileId);
		videoDanmuQuery.setOrderBy("danmu_id asc");

		return getSuccessResponseVO(videoDanmuService.findListByParam(videoDanmuQuery));
	}

}