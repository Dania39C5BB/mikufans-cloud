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
 * 视频播放记录
 */
@Data
public class VideoPlayHistory implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;

	private String videoCover;
	private String videoName;

	@Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，文件索引:"+(fileIndex == null ? "空" : fileIndex)+"，最后更新时间:"+(lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
