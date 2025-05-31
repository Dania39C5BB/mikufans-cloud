package com.linyajin.mikufans.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyajin.mikufans.entity.enums.DateTimePatternEnum;
import com.linyajin.mikufans.utils.DateUtil;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 */
@Data
public class VideoDanmu implements Serializable {


	/**
	 * 自增ID
	 */
	private Integer danmuId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 唯一ID
	 */
	private String fileId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 发布时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date postTime;

	/**
	 * 内容
	 */
	private String text;

	/**
	 * 展示位置
	 */
	private Integer mode;

	/**
	 * 颜色
	 */
	private String color;

	/**
	 * 展示时间
	 */
	private Integer time;

	private String nickName;
	private String videoName;
	private String videoCover;


	@Override
	public String toString (){
		return "自增ID:"+(danmuId == null ? "空" : danmuId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，唯一ID:"+(fileId == null ? "空" : fileId)+"，用户ID:"+(userId == null ? "空" : userId)+"，发布时间:"+(postTime == null ? "空" : DateUtil.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，内容:"+(text == null ? "空" : text)+"，展示位置:"+(mode == null ? "空" : mode)+"，颜色:"+(color == null ? "空" : color)+"，展示时间:"+(time == null ? "空" : time);
	}
}
