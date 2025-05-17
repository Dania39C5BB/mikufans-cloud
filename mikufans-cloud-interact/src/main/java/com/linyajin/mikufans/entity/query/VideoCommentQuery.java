package com.linyajin.mikufans.entity.query;

import lombok.Data;

/**
 * 参数
 */
@Data
public class VideoCommentQuery extends BaseParam {


	/**
	 * 评论ID
	 */
	private Integer commentId;

	/**
	 * 父级评论ID
	 */
	private Integer pCommentId;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 视频用户ID
	 */
	private String videoUserId;

	private String videoUserIdFuzzy;

	/**
	 * 回复内容
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 图片
	 */
	private String imgPath;

	private String imgPathFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 回复人ID
	 */
	private String replyUserId;

	private String replyUserIdFuzzy;

	/**
	 * 0：未置顶 1：置顶
	 */
	private Integer topType;

	/**
	 * 发布时间
	 */
	private String postTime;

	private String postTimeStart;

	private String postTimeEnd;

	/**
	 * 喜欢数量
	 */
	private Integer likeCount;

	/**
	 * 讨厌数量
	 */
	private Integer hateCount;

	private Boolean loadChildren;

	private Boolean queryVideoInfo;

	private String videoNameFuzzy;

}
