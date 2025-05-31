package com.linyajin.mikufans.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.TotalStatisticsInfoDto;
import com.linyajin.mikufans.entity.enums.StatisticsTypeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.UserFocus;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.*;
import com.linyajin.mikufans.mappers.UserFocusMapper;
import com.linyajin.mikufans.mappers.UserInfoMapper;
import com.linyajin.mikufans.mappers.VideoInfoMapper;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.utils.DateUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.po.StatisticsInfo;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.mappers.StatisticsInfoMapper;
import com.linyajin.mikufans.service.StatisticsInfoService;
import com.linyajin.mikufans.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

	@Resource
	private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private VideoInfoMapper<VideoInfo , VideoInfoQuery> videoInfoMapper;

	@Resource
	private UserFocusMapper<UserFocus,UserFocusQuery> userFocusMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<StatisticsInfo> findListByParam(StatisticsInfoQuery param) {
		return this.statisticsInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(StatisticsInfoQuery param) {
		return this.statisticsInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<StatisticsInfo> list = this.findListByParam(param);
		PaginationResultVO<StatisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(StatisticsInfo bean) {
		return this.statisticsInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<StatisticsInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.statisticsInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.statisticsInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param) {
		StringTools.checkParam(param);
		return this.statisticsInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(StatisticsInfoQuery param) {
		StringTools.checkParam(param);
		return this.statisticsInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据StatisticsDateAndUserIdAndDataType获取对象
	 */
	@Override
	public StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
		return this.statisticsInfoMapper.selectByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
	}

	/**
	 * 根据StatisticsDateAndUserIdAndDataType修改
	 */
	@Override
	public Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDataType(StatisticsInfo bean, String statisticsDate, String userId, Integer dataType) {
		return this.statisticsInfoMapper.updateByStatisticsDateAndUserIdAndDataType(bean, statisticsDate, userId, dataType);
	}

	/**
	 * 根据StatisticsDateAndUserIdAndDataType删除
	 */
	@Override
	public Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
		return this.statisticsInfoMapper.deleteByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
	}

	@Override
	public void getStaticsData() {
		List<StatisticsInfo> StatisticsInfoList = new ArrayList<>();

		//统计一天前的数据
		final  String  StatisticsDate = DateUtil.getBeforeDayDate(1);

		//统计播放量
		//easylive:video:playcount:2024-09-28:skO8jtMxVK : 1
		//easylive:video:playcount:2024-09-28:Ko4ypz2m9J : 1
		//它拿到数据之后是上面的形式
		Map<String, Integer> videoPlayCount = redisComponent.getVideoPlayCount(StatisticsDate);
//        将 videoPlayCount 中的所有 Key（即 Redis 键名）提取到一个 List 中。
		List<String> playVideoKeys = new ArrayList<>(videoPlayCount.keySet());
		// 从每个键名中提取 视频ID（即最后一个 : 后的部分） ["skO8jtMxVK", "Ko4ypz2m9J"]  // 仅保留视频ID
		playVideoKeys = playVideoKeys.stream().map(item -> item.substring(item.lastIndexOf(":") + 1)).collect(Collectors.toList());

		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setVideoIds(playVideoKeys.toArray(new String[playVideoKeys.size()]));
		List<VideoInfo> videoInfoList = videoInfoMapper.selectList(videoInfoQuery);
		//按用户ID分组统计播放量
		//1.将 videoInfoList 按视频作者ID（getUserId()）分组
		//2.对每个作者的所有视频 Collectors.summingInt 求和，
		// 累加其播放量（从 videoPlayCountMap 中获取 该键值的播放量
//          ↓ (按用户ID分组求和)
//        videoCountMap: {
//            "用户A": 100,
//            "用户B": 200
//        }
		Map<String, Integer> videoCountMap = videoInfoList.stream().collect(Collectors.groupingBy(VideoInfo::getUserId,
				Collectors.summingInt(item -> videoPlayCount.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT
						+ StatisticsDate + ":" + item.getVideoId()))));
		videoCountMap.forEach((key, value) -> {
			StatisticsInfo statisticsInfo = new StatisticsInfo();
			statisticsInfo.setStatisticsDate(StatisticsDate);
			statisticsInfo.setUserId(key);
			statisticsInfo.setStatisticsCount(value);
			statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
			StatisticsInfoList.add(statisticsInfo);
		});

		//统计粉丝数
		List<StatisticsInfo> fansDataList = statisticsInfoMapper.selectStatisticsFans(StatisticsDate);
		//因为上方已经从数据库查出了粉丝数据，所以此处直接修改类型和统计日期即可
		for ( StatisticsInfo statisticsInfo : fansDataList ) {
			statisticsInfo.setDataType(StatisticsTypeEnum.FANS.getType());
			statisticsInfo.setStatisticsDate(StatisticsDate);
		}
		StatisticsInfoList.addAll(fansDataList);


		//统计评论
		List<StatisticsInfo> commentDataList = statisticsInfoMapper.selectStatisticsComment(StatisticsDate);
		//因为上方已经从数据库查出了粉丝数据，所以此处直接修改类型和统计日期即可
		for ( StatisticsInfo statisticsInfo : commentDataList ) {
			statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
			statisticsInfo.setStatisticsDate(StatisticsDate);
		}
		StatisticsInfoList.addAll(commentDataList);

		//统计点赞，投币，收藏
		List<StatisticsInfo> statisticsOther = statisticsInfoMapper.selectStatisticsOtherInfo(StatisticsDate , new Integer[] {
				UserActionTypeEnum.VIDEO_LIKE.getType(),
				UserActionTypeEnum.VIDEO_COLLECT.getType(),
				UserActionTypeEnum.VIDEO_COIN.getType(),
		});

		for ( StatisticsInfo statisticsInfo : statisticsOther ) {
			statisticsInfo.setStatisticsDate(StatisticsDate);
			if (UserActionTypeEnum.VIDEO_LIKE.getType().equals(statisticsInfo.getDataType())){
				statisticsInfo.setDataType(StatisticsTypeEnum.LIKE.getType());
			} else if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(statisticsInfo.getDataType())){
				statisticsInfo.setDataType(StatisticsTypeEnum.COLLECTION.getType());
			} else if (UserActionTypeEnum.VIDEO_COIN.getType().equals(statisticsInfo.getDataType())){
				statisticsInfo.setDataType(StatisticsTypeEnum.COIN.getType());
			}
		}
		StatisticsInfoList.addAll(statisticsOther);
		statisticsInfoMapper.insertOrUpdateBatch(StatisticsInfoList);

	}

	//获取总的统计信息
	@Override
	public Map<String , Integer> selectAllStatisticsInfo(String userId) {

		TotalStatisticsInfoDto totalStatisticsInfoDto = statisticsInfoMapper.selectTotalStatisticsInfo(userId);

		Map<String, Integer> result = new HashMap<>();
		result.put("likeCount", totalStatisticsInfoDto.getLikeCount());
		result.put("commentCount", totalStatisticsInfoDto.getCommentCount());
		result.put("coinCount", totalStatisticsInfoDto.getCoinCount());
		result.put("collectCount", totalStatisticsInfoDto.getCollectCount());
		result.put("danMuCount", totalStatisticsInfoDto.getDanMuCount());
		result.put("playCount", totalStatisticsInfoDto.getPlayCount());
		//如果userId不为空 证明网页端请求 此时需要求出粉丝的数量
		//如果为null则为管理端请求 此时需要把所有用户的数量求出
		if (!StringTools.isEmpty(userId)) {
			result.put("fansCount", userFocusMapper.selectFansCount(userId));
		} else {
			result.put("userCount", userInfoMapper.selectCount(new UserInfoQuery()));
		}
		return result;
	}

	//获取管理端所有的系统总统计信息
	@Override
	public List<StatisticsInfo> findTotalStatisticsInfo(StatisticsInfoQuery statisticsInfoQuery) {
		return statisticsInfoMapper.selectAdminTotalStatisticsInfo(statisticsInfoQuery);
	}

	//获取管理端的用户统计信息
	@Override
	public List<StatisticsInfo> findUserCountByParam(StatisticsInfoQuery statisticsInfoQuery) {
		return statisticsInfoMapper.selectUserCountByParam(statisticsInfoQuery);
	}
}