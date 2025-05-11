package com.linyajin.mikufans.entity.query;


import lombok.Data;

/**
 * 参数
 */
@Data
public class UserVideoSeriesVideoQuery extends BaseParam {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 排序
	 */
	private Integer sort;

	private Boolean queryVideoInfo;
}
