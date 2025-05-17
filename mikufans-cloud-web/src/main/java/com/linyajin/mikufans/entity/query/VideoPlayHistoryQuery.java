package com.linyajin.mikufans.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 参数
 */
@Data
public class VideoPlayHistoryQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	private String lastUpdateTime;

	private String lastUpdateTimeStart;

	private String lastUpdateTimeEnd;

	private Boolean queryVideoDetail;

}
