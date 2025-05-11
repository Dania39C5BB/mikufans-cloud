package com.linyajin.mikufans.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 参数
 */
@Data
public class UserVideoSeriesQuery extends BaseParam {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 列表名称
	 */
	private String seriesName;

	private String seriesNameFuzzy;

	/**
	 * 描述
	 */
	private String seriesDescription;

	private String seriesDescriptionFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;

}
