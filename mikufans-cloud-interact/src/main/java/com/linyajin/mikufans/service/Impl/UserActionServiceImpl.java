package com.linyajin.mikufans.service.Impl;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.*;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.UserActionMapper;
import com.linyajin.mikufans.mappers.VideoCommentMapper;
import com.linyajin.mikufans.service.UserActionService;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 *  业务接口实现
 */
@Service("userActionService")
public class UserActionServiceImpl implements UserActionService {

	@Resource
	private VideoCommentMapper<VideoComment , VideoCommentQuery> videoCommentMapper;

//	@Resource
//	private VideoInfoMapper<VideoInfo , VideoInfoQuery> videoInfoMapper;

	@Resource
	private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

//	@Resource
//    private UserInfoMapper<UserInfo , UserInfoQuery> userInfoMapper;
//
//	@Resource
//	private EsSearchComponent esSearchComponent;


	@Resource
	private VideoInfoClient videoInfoClient;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserAction> findListByParam(UserActionQuery param) {
		return this.userActionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserActionQuery param) {
		return this.userActionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserAction> findListByPage(UserActionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserAction> list = this.findListByParam(param);
		PaginationResultVO<UserAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserAction bean) {
		return this.userActionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userActionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userActionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserAction bean, UserActionQuery param) {
		StringTools.checkParam(param);
		return this.userActionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserActionQuery param) {
		StringTools.checkParam(param);
		return this.userActionMapper.deleteByParam(param);
	}

	/**
	 * 根据ActionId获取对象
	 */
	@Override
	public UserAction getUserActionByActionId(Integer actionId) {
		return this.userActionMapper.selectByActionId(actionId);
	}

	/**
	 * 根据ActionId修改
	 */
	@Override
	public Integer updateUserActionByActionId(UserAction bean, Integer actionId) {
		return this.userActionMapper.updateByActionId(bean, actionId);
	}

	/**
	 * 根据ActionId删除
	 */
	@Override
	public Integer deleteUserActionByActionId(Integer actionId) {
		return this.userActionMapper.deleteByActionId(actionId);
	}

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId获取对象
	 */
	@Override
	public UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
	}

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId修改
	 */
	@Override
	public Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.updateByVideoIdAndCommentIdAndActionTypeAndUserId(bean, videoId, commentId, actionType, userId);
	}

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId删除
	 */
	@Override
	public Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
		return this.userActionMapper.deleteByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
	}

	//用户点赞、收藏等操作
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void saveUserAction(UserAction userAction) {
		//获取视频信息
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(userAction.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//视频的用户ID
		userAction.setVideoUserId(videoInfo.getUserId());
		//获取要操作的类型
		UserActionTypeEnum actionType = UserActionTypeEnum.getByType(userAction.getActionType());

		if (actionType == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		UserAction dbUserAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(userAction.getVideoId(), userAction.getCommentId(), userAction.getActionType(), userAction.getUserId());

		userAction.setActionTime(new Date());

		switch (actionType) {
			case VIDEO_LIKE:
			case VIDEO_COLLECT:
				if (dbUserAction != null) {
					//数据不等于null就代表数据库里已经对该视频执行过点赞行为
					// 所以需要取消点赞
					userActionMapper.deleteByActionId(dbUserAction.getActionId());
				} else {
					//如果查出的数据为空，则代表数据库里还没有对该视频执行过点赞行为
					userActionMapper.insert(userAction);
				}
				//不管删除还是新增都需要更新视频信息里面的点赞操作收藏的数量
				Integer changeCount = dbUserAction == null ? Constants.ONE : -1;
				videoInfoClient.updateCountInfo(videoInfo.getVideoId(), actionType.getFileId(), changeCount);

				//TODO 更新es的收藏数量
				if (actionType == UserActionTypeEnum.VIDEO_COLLECT) {
//					videoInfoClient.updateDocCount(videoInfo.getVideoId(), SearchOrderTypeEnum.VIDEO_COLLECT.getFileId(), changeCount);
					videoInfoClient.updateDocCount(videoInfo.getVideoId(), SearchOrderTypeEnum.VIDEO_COLLECT, changeCount);
				}
				break;
			case VIDEO_COIN:
				//如果是投币操作
				if (userAction.getUserId().equals(videoInfo.getUserId())) {
					throw new BusinessException("up主不能给自己的视频投币");
				}
				//如果从数据库里面查出的不为空 证明该视频已经投过币了
				if (dbUserAction != null) {
					throw new BusinessException("不能对投过币的视频再次投币");
				}
				//减去自己的硬币数量
				Integer updateCount = videoInfoClient.updateCoinCount(userAction.getUserId(), -userAction.getActionCount());
				if (updateCount == 0) {
					throw new BusinessException("投币失败，硬币不足");
				}
				//给视频所属用户增加硬币数量
				updateCount = videoInfoClient.updateCoinCount(videoInfo.getUserId(), userAction.getActionCount());
				if (updateCount == 0) {
					throw new BusinessException("投币失败");
				}
				userActionMapper.insert(userAction);
				//更新视频信息里面的投币数量
				videoInfoClient.updateCountInfo(videoInfo.getVideoId(), actionType.getFileId(), userAction.getActionCount());
				break;
			case COMMENT_HATE:
			case COMMENT_LIKE:
				//评论点赞/点踩功能实现
				// 确定对立操作类型（点赞的对立是点踩，点踩的对立是点赞）
				UserActionTypeEnum opposeTypeEnum = UserActionTypeEnum.COMMENT_LIKE == actionType ? UserActionTypeEnum.COMMENT_HATE : UserActionTypeEnum.COMMENT_LIKE;
				// 查询用户是否已经执行过对立操作（如果当前是点赞，就查是否点踩过，反之亦然）
				UserAction opposeAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(userAction.getVideoId(), userAction.getCommentId(), opposeTypeEnum.getType(), userAction.getUserId());
				// 如果存在对立操作，先删除对立操作记录
				if (opposeAction != null) {
					userActionMapper.deleteByActionId(opposeAction.getActionId());
				}
				// 再处理当前操作记录 如果当前操作记录存在，则删除；如果不存在，则新增
				if (dbUserAction != null) {
					userActionMapper.deleteByActionId(dbUserAction.getActionId());
				} else {
					userActionMapper.insert(userAction);
				}
				// 计算点赞/点踩数的变化量
				changeCount = dbUserAction == null ? Constants.ONE : -Constants.ONE;

				//对立的操作变化量是当前操作的相反数 比如 点赞（+1）的对立是点踩（-1），所以对立的操作变化量就是-1
				Integer opposeChangeCount  = -changeCount;

				// 更新评论的点赞/点踩计数
				videoCommentMapper.updateCountInfo(userAction.getCommentId(),
						actionType.getFileId(), changeCount ,
						opposeAction == null ? null : opposeTypeEnum.getFileId(),opposeChangeCount);


		}
	}
}