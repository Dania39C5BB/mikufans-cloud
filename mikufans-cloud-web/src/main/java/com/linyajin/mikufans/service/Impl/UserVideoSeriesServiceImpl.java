package com.linyajin.mikufans.service.Impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.po.UserVideoSeriesVideo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.UserVideoSeriesVideoQuery;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.UserVideoSeriesVideoMapper;
import com.linyajin.mikufans.mappers.VideoInfoMapper;
import jakarta.annotation.Resource;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.query.UserVideoSeriesQuery;
import com.linyajin.mikufans.entity.po.UserVideoSeries;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.mappers.UserVideoSeriesMapper;
import com.linyajin.mikufans.service.UserVideoSeriesService;
import com.linyajin.mikufans.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("userVideoSeriesService")
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

	@Resource
	private UserVideoSeriesMapper<UserVideoSeries, UserVideoSeriesQuery> userVideoSeriesMapper;

	@Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	@Resource
	private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param) {
		return this.userVideoSeriesMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserVideoSeriesQuery param) {
		return this.userVideoSeriesMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoSeries> list = this.findListByParam(param);
		PaginationResultVO<UserVideoSeries> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserVideoSeries bean) {
		return this.userVideoSeriesMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserVideoSeries> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserVideoSeries> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoSeriesMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserVideoSeries bean, UserVideoSeriesQuery param) {
		StringTools.checkParam(param);
		return this.userVideoSeriesMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserVideoSeriesQuery param) {
		StringTools.checkParam(param);
		return this.userVideoSeriesMapper.deleteByParam(param);
	}

	/**
	 * 根据SeriesId获取对象
	 */
	@Override
	public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
		return this.userVideoSeriesMapper.selectBySeriesId(seriesId);
	}

	/**
	 * 根据SeriesId修改
	 */
	@Override
	public Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
		return this.userVideoSeriesMapper.updateBySeriesId(bean, seriesId);
	}

	/**
	 * 根据SeriesId删除
	 */
	@Override
	public Integer deleteUserVideoSeriesBySeriesId(Integer seriesId) {
		return this.userVideoSeriesMapper.deleteBySeriesId(seriesId);
	}

	//保存视频到合集中
    @Override
	@GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoSeries(UserVideoSeries bean, String videoIds) {

		//如果集合SeriesId为空，并且视频ID也为空，则不保存
		if (bean.getSeriesId() == null && StringTools.isEmpty(videoIds)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}


		if (bean.getSeriesId() == null) {
			//新增集合检查videoIds中的视频ID是否在数据库中存在
			checkVideoIds(videoIds, bean.getUserId());
			//如果都存在，则新增集合
			bean.setUpdateTime(new Date());

			//根据用户的集合设置集合的排序
			bean.setSort(userVideoSeriesMapper.selectMaxSort(bean.getUserId()) + 1);
			userVideoSeriesMapper.insert(bean);
			saveVideoInSeries(bean.getSeriesId(), bean.getUserId(), videoIds);
		} else {
			//否则就是简单更新视频集合的信息
			UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
			userVideoSeriesQuery.setUserId(bean.getUserId());
			userVideoSeriesQuery.setSeriesId(bean.getSeriesId());

			userVideoSeriesMapper.updateByParam(bean, userVideoSeriesQuery);
		}

    }

	//检查视频ID是否在数据库中存在
	private void checkVideoIds(String videoIds , String userId) {
		String[] ids = videoIds.split(",");

		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setUserId(userId);
		videoInfoQuery.setVideoIds(ids);
		Integer count = videoInfoMapper.selectCount(videoInfoQuery);

		//如果传过来的视频ID数量和数据库中查到的数量不一致，则抛出异常
		if (ids.length != count) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}

	//保存视频到合集中
	@Override
	public void saveVideoInSeries(Integer seriesId, String userId, String videoIds) {

		UserVideoSeries videoSeries = getUserVideoSeriesBySeriesId(seriesId);
		//如果集合SeriesId为空，并且用户ID和查出的集合ID所属者不是同一个人
		if (videoSeries == null || !videoSeries.getUserId().equals(userId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		checkVideoIds(videoIds, userId);

		//分批保存视频到集合中
		String[] ids = videoIds.split(",");

		List<UserVideoSeriesVideo> userVideoSeriesVideoList = new ArrayList<>();

		//设置视频集合中的视频的排序
		Integer sort = userVideoSeriesVideoMapper.selectMaxSort(seriesId);

		for (String videoId : ids) {
			UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
			userVideoSeriesVideo.setUserId(userId);
			userVideoSeriesVideo.setVideoId(videoId);
			userVideoSeriesVideo.setSeriesId(seriesId);
			userVideoSeriesVideo.setSort(++sort);
			userVideoSeriesVideoList.add(userVideoSeriesVideo);
		}

		//批量插入数据
		userVideoSeriesVideoMapper.insertBatch(userVideoSeriesVideoList);

	}

	//获取视频合集列表的第一个视频(封面作用)
	@Override
	public List<UserVideoSeries> getUserVideoSeriesCover(String userId) {
		return userVideoSeriesMapper.getUserVideoSeriesCover(userId);
	}

	//删除集合里面单个视频
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void deleteSeriesInVideo(Integer seriesId, String userId, String videoId) {
		UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
		userVideoSeriesVideoQuery.setUserId(userId);
		userVideoSeriesVideoQuery.setSeriesId(seriesId);
		userVideoSeriesVideoQuery.setVideoId(videoId);
		userVideoSeriesVideoMapper.deleteByParam(userVideoSeriesVideoQuery);
	}

	//更改视频合集的排序
	@Override
	public void changeVideoSeriesSort(String seriesIds, String userId) {

		String[] seriesIdArray = seriesIds.split(",");
		//因为需要批量更新 所以先创建一个集合存储数据
		List<UserVideoSeries> userVideoSeriesList = new ArrayList<>();
		Integer sort = 0;
		//循环遍历需要存储的数据
		for (String seriesId : seriesIdArray) {
			UserVideoSeries userVideoSeries = new UserVideoSeries();
			userVideoSeries.setUserId(userId);
			userVideoSeries.setSort(++sort);
			userVideoSeries.setSeriesId(Integer.parseInt(seriesId));
			userVideoSeriesList.add(userVideoSeries);
		}
		//更新排序
		userVideoSeriesMapper.changeVideoSeriesSort(userVideoSeriesList);

	}

	//获取个人主页的视频合集列表默认加载5个
	@Override
	public List<UserVideoSeries> findUserHomeVideoSeries(UserVideoSeriesQuery userVideoSeriesQuery)
	{
		return userVideoSeriesMapper.findUserHomeVideoSeries(userVideoSeriesQuery);
	}
}