package com.linyajin.mikufans.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 */
@Data
public class UserVideoSeriesVideo implements Serializable {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 排序
	 */
	private Integer sort;

	private String videoName;

	private String videoCover;

	private String playCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	@Override
	public String toString (){
		return "列表ID:"+(seriesId == null ? "空" : seriesId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，用户ID:"+(userId == null ? "空" : userId)+"，排序:"+(sort == null ? "空" : sort);
	}
}
