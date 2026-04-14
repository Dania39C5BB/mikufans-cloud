package com.linyajin.mikufans.service.Impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


import ch.qos.logback.core.util.FileUtil;
import com.linyajin.mikufans.component.EsSearchComponent;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.SysSettingDto;
import com.linyajin.mikufans.dto.UploadingFileDto;
import com.linyajin.mikufans.entity.enums.*;
import com.linyajin.mikufans.entity.po.*;
import com.linyajin.mikufans.entity.query.*;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.*;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.utils.CopyTools;
import com.linyajin.mikufans.utils.FFmpegUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.service.VideoInfoPostService;
import com.linyajin.mikufans.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("videoInfoPostService")
@Slf4j
public class VideoInfoPostServiceImpl implements VideoInfoPostService {

	@Resource
	private EsSearchComponent esSearchComponent;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost , VideoInfoFilePostQuery> videoInfoFilePostMapper;

	@Resource
	private VideoInfoFileMapper<VideoInfoFile , VideoInfoFileQuery> videoInfoFileMapper;

	@Resource
	private FFmpegUtils ffmpegUtils;

    @Resource
    private UserInfoMapper<UserInfo,UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoInfoPost> findListByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoPost> list = this.findListByParam(param);
		PaginationResultVO<VideoInfoPost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoInfoPost bean) {
		return this.videoInfoPostMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfoPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfoPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoInfoPost bean, VideoInfoPostQuery param) {
		StringTools.checkParam(param);
		return this.videoInfoPostMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoInfoPostQuery param) {
		StringTools.checkParam(param);
		return this.videoInfoPostMapper.deleteByParam(param);
	}

	/**
	 * 根据VideoId获取对象
	 */
	@Override
	public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.selectByVideoId(videoId);
	}

	/**
	 * 根据VideoId修改
	 */
	@Override
	public Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId) {
		return this.videoInfoPostMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * 根据VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.deleteByVideoId(videoId);
	}

	//保存视频信息
    @Override
	@GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoInfoPost(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> videoInfoFilePostList) {
		//查看上传的视频分P是否大于系统设置的分P数量
		if (videoInfoFilePostList.size() > redisComponent.getSysSettingDto().getVideoPCount()) {
			throw new BusinessException("上传的分P数量超过系统设置的最大分P数");
		}

		//如果视频ID不为空
		if (!StringTools.isEmpty(videoInfoPost.getVideoId())) {
			VideoInfoPost videoInfoPostDb = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
			if (videoInfoPostDb == null) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

			//在发布投稿的时候如果是 待审核或者转码中的状态则不可以发布
			if (ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUS0.getStatus(),VideoStatusEnum.STATUS2.getStatus()}, videoInfoPostDb.getStatus())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}

		Date curDate = new Date();
		String videoId = videoInfoPost.getVideoId();
		List<VideoInfoFilePost> addFileList = videoInfoFilePostList;
		List<VideoInfoFilePost> deleteFileList = new ArrayList<>();

		//如果视频ID为空 新增视频信息
		if (StringTools.isEmpty(videoId)) {
			videoId = StringTools.getRandomStrUID(10);
			videoInfoPost.setVideoId(videoId);
			videoInfoPost.setCreateTime(curDate);
			videoInfoPost.setLastUpdateTime(curDate);
			videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
			videoInfoPostMapper.insert(videoInfoPost);
		} else {
			//修改
			VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
			videoInfoFilePostQuery.setVideoId(videoId);
			videoInfoFilePostQuery.setUserId(videoInfoPost.getUserId());

			List<VideoInfoFilePost> videoInfoFilePostDb = videoInfoFilePostMapper.selectList(videoInfoFilePostQuery);

			Map<String , VideoInfoFilePost> videoInfoFilePostMap = videoInfoFilePostList.stream().collect(Collectors.toMap(item->item.getUploadId(), Function.identity(), (k1, k2) -> k2));

			Boolean isUpdate = false;
			//对比数据库记录和新上传记录(修改)
			for (VideoInfoFilePost dBFileInfo : videoInfoFilePostDb) {
				//跟新上传的分P文件信息做对比(修改)
				VideoInfoFilePost updateFile = videoInfoFilePostMap.get(dBFileInfo.getFileId());
				//如果在数据库中不存在 则是应该要在数据库中删除的分P文件
				if (updateFile == null) {
					deleteFileList.add(dBFileInfo);
				} else if (!updateFile.getFileName().equals(dBFileInfo.getFileName())) {
					//如果文件名称不一致 则认为是需要更新的分P文件
					isUpdate = true;
				}
			}

			//找出需要新增的分P文件信息 fileId == null 表示是用户新增的分P（因为没有数据库分配的 ID）
			addFileList = videoInfoFilePostList.stream().filter(item ->item.getFileId() == null).toList();

			videoInfoPost.setLastUpdateTime(curDate);

			//查看是否有修改视频信息 比如标题、封面、标签、简介等
			Boolean isEdit = changeVideoInfo(videoInfoPost);
			//如果要添加的文件中不为空 或者有修改视频信息 则更新数据库中的视频信息
			if (addFileList != null && !addFileList.isEmpty()) {
				//把状态设置成转码中状态
				videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
			} else if (isUpdate || isEdit) {
				//如果有修改视频信息 则把状态设置成待审核
				videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
			}
			//更新表中的数据
			videoInfoPostMapper.updateByVideoId(videoInfoPost, videoInfoPost.getVideoId());
		}

		//操作分P文件信息
		if (!deleteFileList.isEmpty()) {
			//根据文件ID删除文件信息
			List<String> fileIdList = deleteFileList.stream().map(item -> item.getFileId()).collect(Collectors.toList());
			videoInfoFilePostMapper.deleteBatchByFileId(fileIdList , videoInfoPost.getUserId());

			//同样需要把本地的临时文件也删除
			List<String> filePathList = deleteFileList.stream().map(item -> item.getFilePath()).collect(Collectors.toList());

			//把要删除的文件保存到redis中
			redisComponent.saveDeleteFileList(filePathList , videoInfoPost.getVideoId());
		}
		//分P文件操作
		Integer index = 1;
		for (VideoInfoFilePost videoInfoFile : videoInfoFilePostList) {
			videoInfoFile.setFileIndex(index++);
			videoInfoFile.setVideoId(videoId);
			videoInfoFile.setUserId(videoInfoPost.getUserId());
			//getFileId == null 表示是新上传的分P文件
			if (videoInfoFile.getFileId() == null) {
				videoInfoFile.setFileId(StringTools.getRandomStrUID(10));
				videoInfoFile.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
				videoInfoFile.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
			}
		}
		videoInfoFilePostMapper.insertOrUpdateBatch(videoInfoFilePostList);

		if (addFileList != null && !addFileList.isEmpty()){
			for (VideoInfoFilePost file : addFileList) {
				file.setVideoId(videoId);
				file.setUserId(videoInfoPost.getUserId());
			}
			redisComponent.saveAddFileList(addFileList);

		}
	}

	public Boolean changeVideoInfo(VideoInfoPost videoInfoPost) {
		VideoInfoPost dbInfo = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
		//如果标题、封面、标签、简介跟数据库里面的不一样 则认为是修改视频信息
		if (!dbInfo.getVideoName().equals(videoInfoPost.getVideoName())
			||!dbInfo.getVideoCover().equals(videoInfoPost.getVideoCover())
			||!dbInfo.getTags().equals(videoInfoPost.getTags())
			|| !dbInfo.getIntroduction().equals(videoInfoPost.getIntroduction() == null ? "" : dbInfo.getIntroduction())) {
			return true;
		} else {
			return false;
		}
	}

	//审核视频
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void auditVideo(String videoId, Integer status, String reason) {
		VideoStatusEnum statusEnum = VideoStatusEnum.getByStatus(status);

		if (statusEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		VideoInfoPost videoInfoPost = new VideoInfoPost();
		videoInfoPost.setStatus(status);

		VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
		videoInfoPostQuery.setVideoId(videoId);
		videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS2.getStatus());

		Integer updateCount = videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
		//数据库语句是 update video_info_post set status = 3 where video_id = ? and status = 2
		//这样做是为了防止并发问题 工作人员打开了两个网页管理端平台  比如视频已经被审核过了
		// 在另一个网页就又被审核了一次 这样就会导致出现问题
		//所以要在更新的时候加上 and status = 2 这样就能保证第一次审核的时候已经更新了数据库
		//在第二个工作人员审核的时候就不会更新了 因为数据库已经被更新过了并且status !=2 所以就不会执行了
		//这时候更新数为0 就会抛出异常提示审核失败 请稍后重试
		if (updateCount == 0) {
			throw new BusinessException("审核失败，请稍后重试");
		}

		//更新文件数据
		VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
		videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.NO_UPDATE.getStatus());

		VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
		videoInfoFilePostQuery.setVideoId(videoId);

		videoInfoFilePostMapper.updateByParam(videoInfoFilePost, videoInfoFilePostQuery);
		//审核失败直接return 就没有后续了
		if (VideoStatusEnum.STATUS4 == statusEnum) {
			return;
		}
		//审核成功之后
		VideoInfoPost dbVideoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);

		VideoInfo dbVideoInfo = videoInfoMapper.selectByVideoId(videoId);
		//证明是用户第一次上传视频
		if (dbVideoInfo == null) {
			//TODO 给用户加积分硬币等
			SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
			userInfoMapper.updateCoinCount(dbVideoInfoPost.getUserId() , sysSettingDto.getPostVideoCoinCount());
		}

		//更新发布信息到正式表
		VideoInfo videoInfo = new VideoInfo();
		BeanUtils.copyProperties(dbVideoInfoPost, videoInfo);
		videoInfoMapper.insertOrUpdate(videoInfo);

		//更新视频文件信息到正式表  先删除再添加
		VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
		videoInfoFileQuery.setVideoId(videoId);
		videoInfoFileMapper.deleteByParam(videoInfoFileQuery);

		//把视频文件复制数据到正式表
		VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
		filePostQuery.setVideoId(videoId);
		//查询出审核成功的视频文件信息 复制到正式表中
		List<VideoInfoFilePost> filePosts = videoInfoFilePostMapper.selectList(filePostQuery);

		List<VideoInfoFile> videoInfoFile = CopyTools.copyList(filePosts, VideoInfoFile.class);

		videoInfoFileMapper.insertBatch(videoInfoFile);


		//发布视频审核成功之后 用户再修改之后比如修改了视频分P文件封面之类的
		//这时候你本地文件已经存了上一次的分p文件了 但是用户删掉了一个分p文件又重新上传了一个新的分p文件
		//web端在发布视频的时候做过一个如果你是修改的并且跟数据库做了对比发现跟上次上传的分P文件不一样
		//它会更新数据库 数据库中未更新的那几个分P文件数据就是要删除本地目录的文件 它会存到redis中
		// 所以你需要从redis中获取这些信息 然后删除本地目录的视频文件
		List<String> delFileList = redisComponent.getDelFileList(videoId);
		if (delFileList != null) {
			for (String delFilePath :delFileList) {
				File file = new File(appConfig.getProjectFolder() + Constants.FILE_DIR + delFilePath);
				if (file.exists()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        log.error("删除文件失败", e);
                    }
                }
			}
		}
		redisComponent.cleanDelFileList(videoId);

		//TODO 保存信息到es中 为什么需要后写es而不先写es
		//为什么需要后写es而不先写es  因为数据库有事务回滚 而es没有事务回滚 所以要先写数据库 再写es
		//如果先写es 再写数据库 假如es写入失败了 这时候肯定会抛出异常 导致数据库写入失败 数据一致
		//如果写es成功了  数据库失败了 es不能回滚 所以就会导致数据不一致
		//如果先写数据库 再写es 假如数据库写入失败了 会回滚 那么es也不会写入
		//会阻断掉es的写入 这样就不会导致数据不一致了
		esSearchComponent.saveDoc(videoInfo);
	}

	//转码任务完成之后更新数据库信息 并且更新视频状态
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void transferVideoInfoFileDb(String videoId, String uploadId, String userId, VideoInfoFilePost videoInfoFilePost) {
		//更新表中的数据
		videoInfoFilePostMapper.updateByUserIdAndUploadId(videoInfoFilePost,userId , uploadId);

		//查看转码失败的视频文件
		VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
		videoInfoFilePostQuery.setVideoId(videoId);
		videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
		//TODO 这里应该改成根据视频id 获取对应的转码结果
		Integer failCount = videoInfoFilePostMapper.selectCount(videoInfoFilePostQuery);
		//如果有转码失败的视频
		if (failCount > 0) {
			//更新视频信息 把状态设置成转码失败
			VideoInfoPost videoInfoPost = new VideoInfoPost();
			videoInfoPost.setStatus(VideoStatusEnum.STATUS1.getStatus());
			videoInfoPostMapper.updateByVideoId(videoInfoPost , videoInfoFilePost.getVideoId());
			return;
		}
		videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
		Integer transferCount = videoInfoFilePostMapper.selectCount(videoInfoFilePostQuery);

		log.info("==========转码中的视频数量==============:{}", transferCount);
		//如果没有转码中的就表示所有视频已经转码完成 把状态设置成待审核
		if (transferCount == 0) {
			//计算所有分p文件的时长总和
			Integer doration = videoInfoFilePostMapper.sumDuration(videoId);
			VideoInfoPost videoInfoPost = new VideoInfoPost();
			//证明视频已经转码完成状态需要设置成待审核状态
			videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
			videoInfoPost.setDuration(doration);
			videoInfoPostMapper.updateByVideoId(videoInfoPost , videoId);
		}
	}
}