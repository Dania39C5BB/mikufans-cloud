package com.linyajin.mikufans.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.linyajin.mikufans.entity.enums.DateTimePatternEnum;
import com.linyajin.mikufans.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 */
@Data
public class StatisticsInfo implements Serializable {


	/**
	 * 统计日期
	 */
	private String statisticsDate;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 数据统计类型
	 */
	private Integer dataType;

	/**
	 * 统计数量
	 */
	private Integer statisticsCount;

}
