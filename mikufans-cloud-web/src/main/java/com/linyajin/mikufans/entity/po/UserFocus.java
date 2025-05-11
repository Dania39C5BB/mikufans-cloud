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
public class UserFocus implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 关注的用户 ID
	 */
	private String focusUserId;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date focusTime;

	private String otherNickName;
	private String otherUserId;
	private String otherAvatar;
	private String otherPersonIntroduction;
	private Integer focusType;
	@Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，关注的用户 ID:"+(focusUserId == null ? "空" : focusUserId)+"，focusTime:"+(focusTime == null ? "空" : DateUtil.format(focusTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
