package com.linyajin.mikufans.entity.query;


import lombok.Data;

/**
 * 参数
 */
@Data
public class VideoInfoFileQuery extends BaseParam {


	/**
	 * 唯一ID
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 文件名称
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件路径
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 持续时间(秒)
	 */
	private Integer duration;

}
