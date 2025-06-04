package com.linyajin.mikufans.entity.query;

import lombok.Data;


/**
 * 参数
 */
@Data
public class VideoDanmuQuery extends BaseParam {


	/**
	 * 自增ID
	 */
	private Integer danmuId;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 唯一ID
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 发布时间
	 */
	private String postTime;

	private String postTimeStart;

	private String postTimeEnd;

	/**
	 * 内容
	 */
	private String text;

	private String textFuzzy;

	/**
	 * 展示位置
	 */
	private Integer mode;

	/**
	 * 颜色
	 */
	private String color;

	private String colorFuzzy;

	/**
	 * 展示时间
	 */
	private Integer time;

	private Boolean queryVideoInfo;

	private String videoUserId;

	private String videoNameFuzzy;
}
