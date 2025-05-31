package com.linyajin.mikufans.service.Impl;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.dto.UserMessageCountDto;
import com.linyajin.mikufans.dto.UserMessageExtendDto;
import com.linyajin.mikufans.entity.enums.MessageReadTypeEnum;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.po.UserMessage;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.*;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.mappers.UserMessageMapper;
import com.linyajin.mikufans.mappers.VideoCommentMapper;
import com.linyajin.mikufans.service.UserMessageService;
import com.linyajin.mikufans.utils.JsonUtils;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 *  业务接口实现
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

	@Resource
	private VideoInfoClient videoInfoClient;

	@Resource
	private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

//	@Resource
//	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	@Resource
	private VideoCommentMapper<VideoComment , VideoCommentQuery> videoCommentMapper;

//	@Resource
//	private VideoInfoPostMapper<VideoInfoPost , VideoInfoPostQuery> videoInfoPostMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserMessage> findListByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserMessage> list = this.findListByParam(param);
		PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserMessage bean) {
		return this.userMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserMessage bean, UserMessageQuery param) {
		StringTools.checkParam(param);
		return this.userMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserMessageQuery param) {
		StringTools.checkParam(param);
		return this.userMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public UserMessage getUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
		return this.userMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.deleteByMessageId(messageId);
	}

	@Override
	@Async
	public void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, String content, Integer replyCommentId) {
		//获取视频所属的用户ID
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoId);
		if (videoInfo == null) {
			return;
		}
		String userId = videoInfo.getUserId();

		UserMessageExtendDto userMessageExtendDto = new UserMessageExtendDto();
		userMessageExtendDto.setMessageContent(content);

		//收藏点赞 已经记录的 不再记录
		if (ArrayUtils.contains(new Integer[] { MessageTypeEnum.LIKE.getType(), MessageTypeEnum.COLLECTION.getType() }, messageTypeEnum.getType())) {
			UserMessageQuery userMessageQuery = new UserMessageQuery();
			userMessageQuery.setUserId(userId);
			userMessageQuery.setVideoId(videoId);
			userMessageQuery.setMessageType(messageTypeEnum.getType());
			Integer count = userMessageMapper.selectCount(userMessageQuery);
			//如果 count 大于0 表示已经记录过了 不再重复记录
			if (count > 0) {
				return;
			}
		}

		UserMessage userMessage = new UserMessage();
		userMessage.setUserId(userId);
		userMessage.setVideoId(videoId);
		userMessage.setMessageType(messageTypeEnum.getType());
		userMessage.setSendUserId(sendUserId);
		userMessage.setCreateTime(new Date());
		userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());

		//评论特殊处理
		//假如是用户是回复评论 那么用户ID应该是当前评论的用户ID 而发送者应该是当前登录的自己
		//比如说 这个视频是你发的 然后有个人给你发了评论 你再去回复这个人的评论
		// 那么这个消息的发送者应该是当前登录的用户 而接收者应该是评论的那个用户
		if (replyCommentId != null) {
			//如果当前的评论数据不为空
			VideoComment videoComment = videoCommentMapper.selectByCommentId(replyCommentId);
			if (videoComment != null) {
				userId = videoComment.getUserId();
				//携带评论的内容
				userMessageExtendDto.setMessageContentReply(videoComment.getContent());
			}
		}

		if (userId.equals(sendUserId)) {
			return;
		}

		//系统消息特殊处理
		if (MessageTypeEnum.SYS == messageTypeEnum) {
			VideoInfoPost videoInfoPost = videoInfoClient.getVideoPostSelectByVideoId(videoId);
			userMessageExtendDto.setAuditStatus(videoInfoPost.getStatus());
		}

		userMessage.setUserId(userId);
		userMessage.setExtendJson(JsonUtils.covertObj2Json(userMessageExtendDto));
		userMessageMapper.insert(userMessage);
	}

	//分组查询 未读消息数量
	@Override
	public List<UserMessageCountDto> getMessageNoReadCount(String userId) {
		return this.userMessageMapper.getMessageNoReadCount(userId);
	}
}