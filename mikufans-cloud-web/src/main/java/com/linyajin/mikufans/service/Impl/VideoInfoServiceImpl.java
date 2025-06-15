package com.linyajin.mikufans.service.Impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.SysSettingDto;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.enums.VideoRecommendTypeEnum;
import com.linyajin.mikufans.entity.po.*;
import com.linyajin.mikufans.entity.query.*;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.*;
import com.linyajin.mikufans.redis.RedisComponent;
import jakarta.annotation.Resource;
import org.apache.catalina.User;
import org.apache.commons.io.FileUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.service.VideoInfoService;
import com.linyajin.mikufans.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("videoInfoService")
public class VideoInfoServiceImpl implements VideoInfoService {

	private static final Logger log = LoggerFactory.getLogger(VideoInfoServiceImpl.class);
	private ExecutorService executorService = Executors.newFixedThreadPool(10);

	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile , VideoInfoFileQuery> videoInfoFileMapper;

	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

//	@Resource
//	private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

//	@Resource
//	private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

	@Resource
    private AppConfig appConfig;

    @Resource
    private UserInfoMapper<UserInfo , UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoInfo> findListByParam(VideoInfoQuery param) {
		return this.videoInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoInfoQuery param) {
		return this.videoInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfo> list = this.findListByParam(param);
		PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoInfo bean) {
		return this.videoInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoInfo bean, VideoInfoQuery param) {
		StringTools.checkParam(param);
		return this.videoInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoInfoQuery param) {
		StringTools.checkParam(param);
		return this.videoInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据VideoId获取对象
	 */
	@Override
	public VideoInfo getVideoInfoByVideoId(String videoId) {
		return this.videoInfoMapper.selectByVideoId(videoId);
	}

	/**
	 * 根据VideoId修改
	 */
	@Override
	public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
		return this.videoInfoMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * 根据VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoByVideoId(String videoId) {
		return this.videoInfoMapper.deleteByVideoId(videoId);
	}

	//保存视频的互动信息设置
    @Override
	@GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoInteraction(String videoId, String userId, String interaction) {
		//更新视频信息数据库表中的互动信息
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.setInteraction(interaction);

		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setVideoId(videoId);
		videoInfoQuery.setUserId(userId);

		videoInfoMapper.updateByParam(videoInfo, videoInfoQuery);

		//更新视频发布表中的互动信息
		VideoInfoPost videoInfoPost = new VideoInfoPost();
		videoInfoPost.setInteraction(interaction);

		VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
		videoInfoPostQuery.setVideoId(videoId);
		videoInfoPostQuery.setUserId(userId);

		videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
	}

	//删除视频
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void deleteVideo(String videoId, String userId) {
		//TODO 管理员在删除的时候也有可能是未审核的视频 所以需要查出发布表的信息
		//TODO 已经发布的时候直接删除就可以了
		VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);

		//如果是管理员删除视频 userId会为空需要额外判断
		//如果 userId 为 null，说明是管理员操作，跳过权限校验（允许删除）
		if (videoInfoPost == null || (userId != null && !userId.equals(videoInfoPost.getUserId()))) {
			throw new BusinessException(ResponseCodeEnum.CODE_404);
		}

		//删除视频信息数据库表中的记录
		videoInfoMapper.deleteByVideoId(videoId);
		//删除视频发布表中的记录
		videoInfoPostMapper.deleteByVideoId(videoId);

		SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
		//TODO 减去用户的硬币
		userInfoMapper.updateCoinCount(videoInfoPost.getUserId(), -sysSettingDto.getPostVideoCoinCount());
		//TODO 删除es信息

		//创建一个线程删除视频、评论、弹幕等分P文件
		//为什么要启用一个线程，因为删除文件耗时较长，如果放在同一个线程中会导致用户等待时间过长
		//和submit相似 不会捕获异常，如果任务抛出异常，会导致线程终止：
		executorService.execute(() -> {
			// 要执行的代码
			VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
			videoInfoFileQuery.setVideoId(videoId);
			//删除视频分p表中的分P文件信息
			videoInfoFileMapper.deleteByParam(videoInfoFileQuery);

			VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
			videoInfoFilePostQuery.setVideoId(videoId);
			//删除视频分P发布表中的分P文件信息
			videoInfoFilePostMapper.deleteByParam(videoInfoFilePostQuery);
			//TODO 调用互动模块删除评论和弹幕
//
//			VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
//			videoDanmuQuery.setVideoId(videoId);
//			//删除视频中的弹幕信息
//			videoDanmuMapper.deleteByParam(videoDanmuQuery);
//
//			VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
//			videoCommentQuery.setVideoId(videoId);
//			//删除视频中的评论信息
//			videoCommentMapper.deleteByParam(videoCommentQuery);

			//以上全部删除完之后 删除本地的文件信息 //TODO 如果改成minio存储同样需要删除minio中的文件信息
			List<VideoInfoFile> videoInfoFiles = videoInfoFileMapper.selectList(videoInfoFileQuery);
			for (VideoInfoFile item:videoInfoFiles) {
                try {
                    FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_DIR + item.getFilePath()));
                } catch (IOException e) {
					log.error("删除本地视频文件失败,文件路径:{}", item.getFilePath());
                }
            }
		});
	}

	//更新视频的播放数量
    @Override
    public void addReadCount(String videoId) {
        videoInfoMapper.updateCountInfo(videoId , UserActionTypeEnum.VIDEO_PLAY.getFileId() ,1);
    }

	//是否推荐视频
	@Override
	public void recommendVideo(String videoId) {
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
		Integer recommend = null;
		//如果是已经推荐的视频 那就设置为不推荐 否则相反
		if (VideoRecommendTypeEnum.RECOMMEND.getType().equals(videoInfo.getRecommendType())) {
			recommend = VideoRecommendTypeEnum.NO_RECOMMEND.getType();
		} else {
			recommend = VideoRecommendTypeEnum.RECOMMEND.getType();
		}

		VideoInfo updateVideoInfo = new VideoInfo();
		updateVideoInfo.setRecommendType(recommend);
		videoInfoMapper.updateByVideoId(updateVideoInfo, videoId);
	}

	//微服务调用更新视频的播放数量等数据
    @Override
    public void updateCountInfo(String videoId, String fileId, Integer changeCount) {
		videoInfoMapper.updateCountInfo(videoId , fileId , changeCount);
    }
}