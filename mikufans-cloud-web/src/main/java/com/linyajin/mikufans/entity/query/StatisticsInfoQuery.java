package com.linyajin.mikufans.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 参数
 */
@Data
public class StatisticsInfoQuery extends BaseParam {


	/**
	 * 统计日期
	 */
	private String statisticsDate;

	private String statisticsDateFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 数据统计类型
	 */
	private Integer dataType;

	private String dateTypeStart;

	private String dateTypeEnd;

	/**
	 * 统计数量
	 */
	private Integer statisticsCount;

	private String statisticsStartDate;

	private String statisticsEndDate;
}
