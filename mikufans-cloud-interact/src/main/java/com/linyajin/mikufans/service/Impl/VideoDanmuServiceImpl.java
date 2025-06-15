package com.linyajin.mikufans.service.Impl;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.VideoDanmu;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.entity.query.VideoDanmuQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.VideoDanmuMapper;
import com.linyajin.mikufans.service.VideoDanmuService;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 *  业务接口实现
 */
@Service("videoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {

//	@Resource
//	private EsSearchComponent esSearchComponent;

	@Resource
	private VideoInfoClient videoInfoClient;

	@Resource
	private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

//	@Resource
//	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoDanmu> findListByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoDanmu> list = this.findListByParam(param);
		PaginationResultVO<VideoDanmu> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoDanmu bean) {
		return this.videoDanmuMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoDanmu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoDanmu bean, VideoDanmuQuery param) {
		StringTools.checkParam(param);
		return this.videoDanmuMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoDanmuQuery param) {
		StringTools.checkParam(param);
		return this.videoDanmuMapper.deleteByParam(param);
	}

	/**
	 * 根据DanmuId获取对象
	 */
	@Override
	public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
		return this.videoDanmuMapper.selectByDanmuId(danmuId);
	}

	/**
	 * 根据DanmuId修改
	 */
	@Override
	public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
		return this.videoDanmuMapper.updateByDanmuId(bean, danmuId);
	}

	/**
	 * 根据DanmuId删除
	 */
	@Override
	public Integer deleteVideoDanmuByDanmuId(Integer danmuId) {
		return this.videoDanmuMapper.deleteByDanmuId(danmuId);
	}

	/**
	 * 发布弹幕
	 * @param videoDanmu 弹幕信息
	 */
    @Override
	@GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoDanMu(VideoDanmu videoDanmu) {
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoDanmu.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//查询该视频是否关闭弹幕或者评论
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
			throw new BusinessException("该视频已经关闭弹幕");
		}
		//新增弹幕信息
		videoDanmuMapper.insert(videoDanmu);

		//更新弹幕数量
		videoInfoClient.updateCountInfo(videoDanmu.getVideoId(), UserActionTypeEnum.VIDEO_DANMU.getFileId(), 1);
		//TODO 	更新es 弹幕数量
//		videoInfoClient.updateDocCount(videoDanmu.getVideoId() , SearchOrderTypeEnum.VIDEO_DANMU.getFileId(), 1);
		videoInfoClient.updateDocCount(videoDanmu.getVideoId() , SearchOrderTypeEnum.VIDEO_DANMU, 1);
	}

	//删除弹幕
	@Override
	public void deleteDanMu(Integer danMuId, String userId) {
		//查询该弹幕在数据库是否存在
		VideoDanmu videoDanmu = videoDanmuMapper.selectByDanmuId(danMuId);
		if (videoDanmu == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//查询该弹幕在哪个视频中发送的
		VideoInfo videoInfo = videoInfoClient.getVideoSelectByVideoId(videoDanmu.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//直接删除弹幕会导致 越权问题：任何用户只要知道 danMuId
		// 就可以删除任意弹幕（即使不是自己的视频）
		// 所以需要查出视频信息然后根据视频信息中的用户ID来判断是否允许删除弹幕
		//如果不是管理员并且不是视频发布者，则不允许删除弹幕
		if (userId != null && !userId.equals(videoInfo.getUserId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		videoDanmuMapper.deleteByDanmuId(danMuId);
	}
}