package com.linyajin.mikufans.service.Impl;

import java.util.Date;
import java.util.List;

import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.query.UserInfoQuery;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.UserInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.query.UserFocusQuery;
import com.linyajin.mikufans.entity.po.UserFocus;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.mappers.UserFocusMapper;
import com.linyajin.mikufans.service.UserFocusService;
import com.linyajin.mikufans.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("userFocusService")
public class UserFocusServiceImpl implements UserFocusService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserFocus> findListByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserFocusQuery param) {
		return this.userFocusMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserFocus> list = this.findListByParam(param);
		PaginationResultVO<UserFocus> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserFocus bean) {
		return this.userFocusMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserFocus> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserFocus> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userFocusMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserFocus bean, UserFocusQuery param) {
		StringTools.checkParam(param);
		return this.userFocusMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserFocusQuery param) {
		StringTools.checkParam(param);
		return this.userFocusMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndFocusUserId获取对象
	 */
	@Override
	public UserFocus getUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
		return this.userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
	}

	/**
	 * 根据UserIdAndFocusUserId修改
	 */
	@Override
	public Integer updateUserFocusByUserIdAndFocusUserId(UserFocus bean, String userId, String focusUserId) {
		return this.userFocusMapper.updateByUserIdAndFocusUserId(bean, userId, focusUserId);
	}

	/**
	 * 根据UserIdAndFocusUserId删除
	 */
	@Override
	public Integer deleteUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
		return this.userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
	}

	//关注用户
	@Override
	public void focusUser(String userId, String focusUserId) {
		//如果关注人跟被关注人相同
		if (userId.equals(focusUserId)) {
			throw new BusinessException("不能关注自己");
		}

		//查询是否已经关注
		UserFocus focusInfo = userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
		if (focusInfo != null) {
			return;
		}

		//查询被关注人是否存在
		UserInfo dbUserInfo = userInfoMapper.selectByUserId(focusUserId);
		if (dbUserInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserFocus focus = new UserFocus();
		focus.setUserId(userId);
		focus.setFocusUserId(focusUserId);
		focus.setFocusTime(new Date());
		userFocusMapper.insert(focus);
	}

	//取消关注用户
	@Override
	public void cancelFocusUser(String userId, String focusUserId) {
		deleteUserFocusByUserIdAndFocusUserId(userId, focusUserId);
	}
}