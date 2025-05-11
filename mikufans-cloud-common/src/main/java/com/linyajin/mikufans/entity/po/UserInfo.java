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
public class UserInfo implements Serializable {

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 0：女 1：男 2：未知
	 */
	private Integer sex;

	/**
	 * 出生日期
	 */
	private String birthday;

	/**
	 * 学校
	 */
	private String school;

	/**
	 * 个人简介
	 */
	private String personIntroduction;

	/**
	 * 加入时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date joinTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 最后登录IP
	 */
	private String lastLoginIp;

	/**
	 * 0：禁用 1：正常
	 */
	private Integer status;

	/**
	 * 空间公告
	 */
	private String noticeInfo;

	/**
	 * 硬币总数量
	 */
	private Integer totalCoinCount;

	/**
	 * 当前硬币数量
	 */
	private Integer currentCoinCount;

	/**
	 * 主题
	 */
	private Integer theme;

	//头像
	private String avatar;

	/**
	 * 粉丝数量
	 */
	private Integer fansCount;

	/**
	 * 关注数量
	 */
	private Integer focusCount;

	/**
	 * 喜欢的数量
	 */
	private Integer likeCount;

	/**
	 * 播放数量
	 */
	private Integer playCount;

	/**
	 * 是否关注
	 */
	private Boolean haveFocus;


	@Override
	public String toString (){
		return "用户id:"+(userId == null ? "空" : userId)+"，昵称:"+(nickName == null ? "空" : nickName)+"，邮箱:"+(email == null ? "空" : email)+"，密码:"+(password == null ? "空" : password)+"，0：女 1：男 2：未知:"+(sex == null ? "空" : sex)+"，出生日期:"+(birthday == null ? "空" : birthday)+"，学校:"+(school == null ? "空" : school)+"，个人简介:"+(personIntroduction == null ? "空" : personIntroduction)+"，加入时间:"+(joinTime == null ? "空" : DateUtil.format(joinTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登录时间:"+(lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登录IP:"+(lastLoginIp == null ? "空" : lastLoginIp)+"，0：禁用 1：正常:"+(status == null ? "空" : status)+"，空间公告:"+(noticeInfo == null ? "空" : noticeInfo)+"，硬币总数量:"+(totalCoinCount == null ? "空" : totalCoinCount)+"，当前硬币数量:"+(currentCoinCount == null ? "空" : currentCoinCount)+"，主题:"+(theme == null ? "空" : theme);
	}
}
