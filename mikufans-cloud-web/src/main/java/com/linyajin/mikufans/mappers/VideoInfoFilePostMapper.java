package com.linyajin.mikufans.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface VideoInfoFilePostMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据FileId更新
	 */
	 Integer updateByFileId(@Param("bean") T t,@Param("fileId") String fileId);


	/**
	 * 根据FileId删除
	 */
	 Integer deleteByFileId(@Param("fileId") String fileId);


	/**
	 * 根据FileId获取对象
	 */
	 T selectByFileId(@Param("fileId") String fileId);


	/**
	 * 根据UserIdAndUploadId更新
	 */
	 Integer updateByUserIdAndUploadId(@Param("bean") T t,@Param("userId") String userId,@Param("uploadId") String uploadId);


	/**
	 * 根据UserIdAndUploadId删除
	 */
	 Integer deleteByUserIdAndUploadId(@Param("userId") String userId,@Param("uploadId") String uploadId);


	/**
	 * 根据UserIdAndUploadId获取对象
	 */
	 T selectByUserIdAndUploadId(@Param("userId") String userId,@Param("uploadId") String uploadId);


    void deleteBatchByFileId(@Param("fileIdList") List<String> fileIdList, @Param("userId") String userId);

    Integer sumDuration(@Param("videoId")  String videoId);
}
