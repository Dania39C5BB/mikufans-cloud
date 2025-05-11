package com.linyajin.mikufans.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 参数
 */
@Data
public class UserFocusQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 关注的用户 ID
	 */
	private String focusUserId;

	private String focusUserIdFuzzy;

	/**
	 * 
	 */
	private String focusTime;

	private String focusTimeStart;

	private String focusTimeEnd;

	/**
	 * 查询类型 0: 查询关注列表,1: 查询粉丝列表
	 */
	private Integer queryType;

}
