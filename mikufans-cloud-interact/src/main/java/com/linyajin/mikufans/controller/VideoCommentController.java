package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.enums.CommentTopTypeEnum;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.UserActionQuery;
import com.linyajin.mikufans.entity.query.VideoCommentQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.entity.vo.VideoCommentResultVO;
import com.linyajin.mikufans.service.UserActionService;
import com.linyajin.mikufans.service.VideoCommentService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *  Controller
 */
@RestController
@Validated
@RequestMapping("/comment")
public class VideoCommentController extends ABaseController{


	@Resource
	private VideoCommentService videoCommentService;

	@Resource
	private UserActionService userActionService;

	@Resource
	private VideoInfoClient videoInfoClient;

	/**
	 * postComment 发布评论
	 * @param videoId 视频id
	 * @param content 评论内容
	 * @param replyCommentId 回复评论id
	 * @param imgPath 头像图片路径
	 * @return ResponseVO
	 */
	@PostMapping("/postComment")
	@GlobalInterceptor(checkLogin = true)
	@RecordUserMessage(messageType = MessageTypeEnum.COMMENT)
	public ResponseVO postComment(@NotEmpty String videoId ,
								  @NotEmpty @Size(max = 500) String content,
								  Integer replyCommentId,
								  @Size(max = 50) String imgPath){

		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
		VideoComment videoComment = new VideoComment();
		videoComment.setVideoId(videoId);
		videoComment.setContent(content);
		videoComment.setImgPath(imgPath);
		videoComment.setUserId(tokenUserInfoDto.getUserId());
		videoComment.setNickName(tokenUserInfoDto.getNickName());
		videoCommentService.postComment(videoComment,replyCommentId);
		return getSuccessResponseVO(videoComment);
    }


	/**
	 *
	 * @param videoId 视频id
	 * @param orderType 排序类型
	 * @param PageNo 分页的页码
	 * @return ResponseVO
	 */
	@GetMapping("/loadComment")
	public ResponseVO loadComment(@NotEmpty String videoId ,
								  Integer orderType,
								  Integer PageNo){
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoId);
		//如果关闭评论，则不返回数据
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
			return getSuccessResponseVO(new ArrayList<>());
		}

		VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
		videoCommentQuery.setLoadChildren(true);
		videoCommentQuery.setVideoId(videoId);
		videoCommentQuery.setPCommentId(0);
		videoCommentQuery.setPageNo(PageNo);
		videoCommentQuery.setPageSize(PageSize.SIZE15.getSize());
		//排序规则 orderType = 0 最新评论在前
		String orderBy = orderType == null || orderType == 0 ? "like_count desc , comment_id desc" : "comment_id desc";
		videoCommentQuery.setOrderBy(orderBy);
		PaginationResultVO<VideoComment> commentData = videoCommentService.findListByPage(videoCommentQuery);

		if (PageNo == null || PageNo == 0) {
			// 单独获取置顶评论列表
			List<VideoComment> topCommentList = topCommentList(videoId);
			// 如果存在置顶评论
			if (!topCommentList.isEmpty()) {
				// 从当前评论列表中过滤出与置顶评论ID不相同的评论
				List<VideoComment> collectList = commentData.getList().stream().filter(item -> !item.getCommentId().equals(topCommentList.get(0).getCommentId())).collect(Collectors.toList());
				// 将置顶评论添加到列表开头位置
				collectList.addAll(0,topCommentList);
				// 更新评论数据列表
				commentData.setList(collectList);
			}
		}


		VideoCommentResultVO resultVO = new VideoCommentResultVO();
		resultVO.setCommentData(commentData);

		//查询当前登录的用户操作该视频评论区的行为
		List<UserAction> userActionList = new ArrayList<>();
		TokenUserInfoDto userInfoDto = getTokenUserInfoDto();
		if (userInfoDto != null) {
			UserActionQuery userActionQuery = new UserActionQuery();
			userActionQuery.setVideoId(videoId);
			userActionQuery.setUserId(userInfoDto.getUserId());
			userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType() , UserActionTypeEnum.COMMENT_HATE.getType()});
			userActionList = userActionService.findListByParam(userActionQuery);
		}
		resultVO.setUserActionList(userActionList);
		return getSuccessResponseVO(resultVO);
	}

	//查询置顶评论
	private List<VideoComment> topCommentList(String videoId) {
		VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
		videoCommentQuery.setVideoId(videoId);
		videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
		videoCommentQuery.setLoadChildren(true);
		List<VideoComment> commentList = videoCommentService.findListByParam(videoCommentQuery);
		return  commentList;
	}

	/**
	 * 置顶评论
	 * @param commentId 评论id
	 * @return ResponseVO
	 */
	@GetMapping("/topComment")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO topComment(@NotNull Integer commentId){
		TokenUserInfoDto userInfoDto = getTokenUserInfoDto();
		videoCommentService.topComment(commentId , userInfoDto.getUserId());
		return getSuccessResponseVO(null);
	}

	/**
	 * 取消置顶评论
	 * @param commentId 评论id
	 * @return ResponseVO
	 */
	@GetMapping("/cancelTopComment")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO cancelTopComment(@NotNull Integer commentId){
		TokenUserInfoDto userInfoDto = getTokenUserInfoDto();
		videoCommentService.cancelTopComment(commentId , userInfoDto.getUserId());
		return getSuccessResponseVO(null);
	}

	/**
	 * 删除评论
	 * @param commentId 评论id
	 * @return ResponseVO
	 */
	@DeleteMapping("/userDelComment")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO userDelComment(@NotNull Integer commentId){
		TokenUserInfoDto userInfoDto = getTokenUserInfoDto();
		videoCommentService.delComment(commentId , userInfoDto.getUserId());
		return getSuccessResponseVO(null);
	}
}