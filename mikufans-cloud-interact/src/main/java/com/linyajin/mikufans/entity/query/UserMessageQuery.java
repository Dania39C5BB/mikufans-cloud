package com.linyajin.mikufans.entity.query;

import lombok.Data;


/**
 * 参数
 */
@Data
public class UserMessageQuery extends BaseParam {


	/**
	 * 消息ID自增
	 */
	private Integer messageId;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 主体ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 消息类型
	 */
	private Integer messageType;

	/**
	 * 发送人ID
	 */
	private String sendUserId;

	private String sendUserIdFuzzy;

	/**
	 * 0：未读 1：已读
	 */
	private Integer readType;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 扩展信息
	 */
	private String extendJson;

	private String extendJsonFuzzy;

}
