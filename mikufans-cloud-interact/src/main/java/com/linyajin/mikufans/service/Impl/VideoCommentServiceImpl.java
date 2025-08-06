package com.linyajin.mikufans.service.Impl;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.CommentTopTypeEnum;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.po.VideoComment;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.entity.query.VideoCommentQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.VideoCommentMapper;
import com.linyajin.mikufans.service.VideoCommentService;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 *  业务接口实现
 */
@Service("videoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

	private static final Logger log = LoggerFactory.getLogger(VideoCommentServiceImpl.class);
//	@Resource
//	private UserInfoMapper<UserInfo , UserInfoQuery> userInfoMapper;
//	@Resource
//	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	@Resource
	private VideoInfoClient videoInfoClient;

	@Resource
	private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoComment> findListByParam(VideoCommentQuery param) {
		if (param.getLoadChildren() !=null && param.getLoadChildren()) {
			return this.videoCommentMapper.selectListWithChildren(param);
		}
		return this.videoCommentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoCommentQuery param) {
		return this.videoCommentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoComment> list = this.findListByParam(param);
		PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoComment bean) {
		return this.videoCommentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoComment bean, VideoCommentQuery param) {
		StringTools.checkParam(param);
		return this.videoCommentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoCommentQuery param) {
		StringTools.checkParam(param);
		return this.videoCommentMapper.deleteByParam(param);
	}

	/**
	 * 根据CommentId获取对象
	 */
	@Override
	public VideoComment getVideoCommentByCommentId(Integer commentId) {
		return this.videoCommentMapper.selectByCommentId(commentId);
	}

	/**
	 * 根据CommentId修改
	 */
	@Override
	public Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
		return this.videoCommentMapper.updateByCommentId(bean, commentId);
	}

	/**
	 * 根据CommentId删除
	 */
	@Override
	public Integer deleteVideoCommentByCommentId(Integer commentId) {
		return this.videoCommentMapper.deleteByCommentId(commentId);
	}

	//发布评论
    @Override
	@GlobalTransactional(rollbackFor = Exception.class)
    public void postComment(VideoComment videoComment, Integer replyCommentId) {
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoComment.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
			throw new BusinessException("用户已关闭评论区");
		}
		// 如果replyCommentId不为空，表示这是对某条评论的回复
		if (replyCommentId != null) {
			// 查询被回复的评论信息
			VideoComment replayComment = getVideoCommentByCommentId(replyCommentId);
			// 验证被回复的评论是否存在且属于同一个视频
			if (replayComment == null || !replayComment.getVideoId().equals(videoComment.getVideoId())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			// 如果被回复评论的父评论ID是0，表示这是一级评论
			if (replayComment.getPCommentId() == 0) {
				// 设置当前评论的父评论ID为被回复评论的ID，建立二级评论关系
				videoComment.setPCommentId(replayComment.getCommentId());
			} else {
				//被回复的评论本身已经是二级（或更深层）评论
				//当前评论的父评论ID（pCommentId）应该指向被回复评论的父评论ID（replayComment.getPCommentId()）
				// 这样保持评论的层级结构，避免无限嵌套的层级关系问题
				videoComment.setPCommentId(replayComment.getPCommentId());
				// 同时，记录当前评论是在回复哪个用户（被回复评论的作者）
				// 用于前端显示"回复@用户名"的效果
				videoComment.setReplyUserId(replayComment.getUserId());
			}
			// 根据被回复评论的用户ID(replayComment.getUserId())查询用户信息
			UserInfo userInfo = videoInfoClient.selectByUserId(replayComment.getUserId());
			// 将被回复用户的昵称设置到当前评论对象中
			// 用于前端显示"回复@用户名"时展示正确的昵称
			videoComment.setReplyNickName(userInfo.getNickName());
			//将被回复用户的头像URL设置到当前评论对象中
			// 用于前端可能需要展示的回复用户头像
			videoComment.setReplyAvatar(userInfo.getAvatar());

		} else {
			videoComment.setPCommentId(0);
		}

		videoComment.setPostTime(new Date());
		//当前评论属于哪一个视频的用户发布的
		videoComment.setVideoUserId(videoInfo.getUserId());

		videoCommentMapper.insert(videoComment);

		//更新视频评论区的数量
//		if (videoComment.getPCommentId() == 0) {
		// 无论是一级评论还是二级评论，都更新视频评论数
		videoInfoClient.updateCountInfo(videoComment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getFileId(), 1);
//		}
	}

	//置顶评论
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void topComment(Integer commentId, String userId) {
		//置顶之前先取消之前的置顶评论
		this.cancelTopComment(commentId ,userId);
		VideoComment videoComment = new VideoComment();
		videoComment.setTopType(CommentTopTypeEnum.TOP.getType());
		videoCommentMapper.updateByCommentId(videoComment, commentId);
	}


	//取消置顶评论
	@Override
	public void cancelTopComment(Integer commentId, String userId) {
		VideoComment dbComment = videoCommentMapper.selectByCommentId(commentId);
		if (dbComment == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(dbComment.getVideoId());
		//如果删除评论的用户不是发布视频的用户，则不允许取消置顶
		if (videoInfo == null || !videoInfo.getUserId().equals(userId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		//要更新的数据
		VideoComment videoComment = new VideoComment();
		videoComment.setTopType(CommentTopTypeEnum.NO_TOP.getType());

		//更新条件
		VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
		videoCommentQuery.setVideoId(dbComment.getVideoId());
		videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
		//把已置顶的评论改为未置顶
		videoCommentMapper.updateByParam(videoComment , videoCommentQuery);


	}

	//删除评论
	@Override
	public void delComment(Integer commentId, String userId) {
		VideoComment dbComment = videoCommentMapper.selectByCommentId(commentId);
		if (dbComment == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(dbComment.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//如果删除评论的人不是发布视频的用户并且不是评论的发布者，则不允许删除评论
		if (userId != null && !videoInfo.getUserId().equals(userId) && !dbComment.getUserId().equals(userId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		//删除评论
		Integer delPCommentCount = videoCommentMapper.deleteByCommentId(commentId);
		log.info("父级评论删除的数量：{}", delPCommentCount);

		//如果是父级评论，则删除子评论
		if (dbComment.getPCommentId() == 0) {
			VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
			videoCommentQuery.setPCommentId(commentId);
			Integer childCommentCount = videoCommentMapper.deleteByParam(videoCommentQuery);
			log.info("子级评论删除的数量：{}", childCommentCount);
			//更新视频评论数
			videoInfoClient.updateCountInfo(dbComment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getFileId(),-(childCommentCount + delPCommentCount) );
		}
	}

}