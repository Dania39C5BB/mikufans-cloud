package com.linyajin.mikufans.mappers;

import com.linyajin.mikufans.dto.TotalStatisticsInfoDto;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *  数据库操作接口
 */
public interface StatisticsInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据StatisticsDateAndUserIdAndDataType更新
	 */
	 Integer updateByStatisticsDateAndUserIdAndDataType(@Param("bean") T t,@Param("statisticsDate") String statisticsDate,@Param("userId") String userId,@Param("dataType") Integer dataType);


	/**
	 * 根据StatisticsDateAndUserIdAndDataType删除
	 */
	 Integer deleteByStatisticsDateAndUserIdAndDataType(@Param("statisticsDate") String statisticsDate,@Param("userId") String userId,@Param("dataType") Integer dataType);


	/**
	 * 根据StatisticsDateAndUserIdAndDataType获取对象
	 */
	 T selectByStatisticsDateAndUserIdAndDataType(@Param("statisticsDate") String statisticsDate,@Param("userId") String userId,@Param("dataType") Integer dataType);


	 //统计粉丝数
	List<T> selectStatisticsFans(@Param("statisticsDate") String statisticsDate);
	//统计评论数
	List<T> selectStatisticsComment(@Param("statisticsDate") String statisticsDate);
	//其他信息统计 点赞 收藏等
	List<T> selectStatisticsOtherInfo(@Param("statisticsDate") String statisticsDate, @Param("actionTypeArray") Integer[] actionType);

	//查询总的统计信息
	TotalStatisticsInfoDto selectTotalStatisticsInfo(@Param("userId") String userId);

	//查询管理端总的统计信息
	List<T> selectAdminTotalStatisticsInfo(@Param("query") P p);
	//获取管理端的用户统计信息
	List<T> selectUserCountByParam(@Param("query") P p);
}
