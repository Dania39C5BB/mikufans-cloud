package com.linyajin.mikufans.service;

import java.util.List;

import com.linyajin.mikufans.entity.query.VideoPlayHistoryQuery;
import com.linyajin.mikufans.entity.po.VideoPlayHistory;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface VideoPlayHistoryService {

	/**
	 * 根据条件查询列表
	 */
	List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(VideoPlayHistoryQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoPlayHistory bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoPlayHistory> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoPlayHistory> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(VideoPlayHistory bean,VideoPlayHistoryQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(VideoPlayHistoryQuery param);

	/**
	 * 根据UserId查询对象
	 */
	VideoPlayHistory getVideoPlayHistoryByUserId(String userId);


	/**
	 * 根据UserId修改
	 */
	Integer updateVideoPlayHistoryByUserId(VideoPlayHistory bean,String userId);


	/**
	 * 根据UserId删除
	 */
	Integer deleteVideoPlayHistoryByUserId(String userId);

	Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);
}