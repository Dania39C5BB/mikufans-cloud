package com.linyajin.mikufans.service.Impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.*;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.enums.UserSexEnum;
import com.linyajin.mikufans.entity.enums.UserStatusEnum;
import com.linyajin.mikufans.entity.po.UserFocus;
import com.linyajin.mikufans.entity.query.UserFocusQuery;
import com.linyajin.mikufans.dto.UserCountInfoDto;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.UserFocusMapper;
import com.linyajin.mikufans.mappers.VideoInfoMapper;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.service.UserFocusService;
import com.linyajin.mikufans.utils.CopyTools;
import jakarta.annotation.Resource;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.query.UserInfoQuery;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.mappers.UserInfoMapper;
import com.linyajin.mikufans.service.UserInfoService;
import com.linyajin.mikufans.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据NickName获取对象
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * 根据NickName修改
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return this.userInfoMapper.updateByNickName(bean, nickName);
	}

	/**
	 * 根据NickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);
	}

	//注册账号
	@Override
	public void register(UserInfoDto userInfoDto) {
		String email = userInfoDto.getEmail();
		String nickName = userInfoDto.getNickName();

		UserInfo userInfoEmail = userInfoMapper.selectByEmail(email);
		if (userInfoEmail != null) {
			throw new BusinessException("邮箱已被注册");
		}

		UserInfo userInfoName = userInfoMapper.selectByNickName(nickName);
		if (userInfoName != null) {
			throw new BusinessException("昵称已被使用");
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setEmail(email);
		userInfo.setUserId(StringTools.getRandomIntUID(10));
		userInfo.setNickName(nickName);
		userInfo.setPassword(StringTools.encodeByMd5(userInfoDto.getPassword()));
		userInfo.setJoinTime(new Date());
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setSex(UserSexEnum.SECRECY.getType());
		userInfo.setTheme(Constants.THEME_ONE);
		//初始化硬币数量
		SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
		userInfo.setTotalCoinCount(sysSettingDto.getRegisterCoinCount());
		userInfo.setCurrentCoinCount(sysSettingDto.getRegisterCoinCount());
		userInfoMapper.insert(userInfo);
	}

	//登录账号
	@Override
	public TokenUserInfoDto login(LoginUserDto loginUserInfoDto, String ip) {
		String email = loginUserInfoDto.getEmail();
		String password = StringTools.encodeByMd5(loginUserInfoDto.getPassword());

		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		if (userInfo == null || !password.equals(userInfo.getPassword())) {
			throw new BusinessException("帐号或密码错误");
		}

		if (!UserStatusEnum.ENABLE.getStatus().equals(userInfo.getStatus())) {
			throw new BusinessException("帐号已被禁用");
		}

		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		updateInfo.setLastLoginIp(ip);

		userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());

		TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();


		BeanUtils.copyProperties(userInfo , tokenUserInfoDto);

		//生成token和登录信息保存到redis中
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("userId", userInfo.getUserId());
		claims.put("nickName", userInfo.getNickName());

		redisComponent.saveTokenUserInfo(tokenUserInfoDto, claims);

		return tokenUserInfoDto;

	}

	//获取用户信息
	//currentUserId 当前登录的用户ID userId 需要获取用户信息的用户ID
    @Override
    public UserInfo getUserDetailsInfo(String currentUserId, String userId) {
		UserInfo userInfo = getUserInfoByUserId(userId);
		if (userInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_404);
		}
		//TODO  播放数  点赞数
		CountInfoDto countInfoDto = videoInfoMapper.selectSumCountInfo(userId);

		CopyTools.copyProperties(countInfoDto, userInfo);
		//查询粉丝数量
		Integer fansCount = userFocusMapper.selectFansCount(userId);
		//查询关注数量
		Integer focusCount = userFocusMapper.selectFocusCount(userId);
		userInfo.setFansCount(fansCount);
		userInfo.setFocusCount(focusCount);
		//查询当前登录的用户是否关注了对方
		if (currentUserId == null) {
			userInfo.setHaveFocus(false);
		} else {
			UserFocus dbFocus = userFocusMapper.selectByUserIdAndFocusUserId(currentUserId, userId);
			userInfo.setHaveFocus(dbFocus == null ? false : true);
		}

		return userInfo;
    }

	//更新用户信息
	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public void updateUserInfo(UserInfo userInfo, TokenUserInfoDto tokenUserInfoDto) {
		UserInfo dbUserInfo = userInfoMapper.selectByUserId(userInfo.getUserId());
		//判断修改昵称时硬币数量是否足够并且必须跟之前的昵称不一样
		if (!dbUserInfo.getNickName().equals(userInfo.getNickName()) && dbUserInfo.getCurrentCoinCount() < Constants.UPDATE_NICKNAME_COIN_COUNT) {
			throw new BusinessException("修改昵称失败，硬币不足");
		}

		//到这里说明硬币足够，并且昵称不一样，需要扣除硬币
		if (!dbUserInfo.getNickName().equals(userInfo.getNickName())) {
			Integer count = userInfoMapper.updateCoinCount(userInfo.getUserId(), -Constants.UPDATE_NICKNAME_COIN_COUNT);
			if (count == 0) {
				throw new BusinessException("修改昵称失败，硬币数量不足");
			}
		}
		//更新用户信息
		userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

		//更新redis中的用户信息
		Boolean updateUserInfo = false;

		if (!userInfo.getNickName().equals(tokenUserInfoDto.getNickName())) {
			tokenUserInfoDto.setNickName(userInfo.getNickName());
			updateUserInfo = true;
		}

		if (!userInfo.getAvatar().equals(tokenUserInfoDto.getAvatar())) {
			tokenUserInfoDto.setAvatar(userInfo.getAvatar());
			updateUserInfo = true;
		}

		if (updateUserInfo) {
			redisComponent.updateTokenUserInfo(tokenUserInfoDto);
		}
	}

	//查询用户 粉丝/硬币/关注数量
    @Override
    public UserCountInfoDto getUserCountInfo(String userId) {
		UserInfo userInfo = getUserInfoByUserId(userId);
		Integer fansCount = userFocusMapper.selectFansCount(userId);
		Integer focusCount = userFocusMapper.selectFocusCount(userId);
		UserCountInfoDto userCountInfoDto = new UserCountInfoDto();
		userCountInfoDto.setFansCount(fansCount);
		userCountInfoDto.setFocusCount(focusCount);
		userCountInfoDto.setCurrentCoinCount(userInfo.getCurrentCoinCount());
		return userCountInfoDto;
    }


	//微服务调用更新硬币数量
    @Override
    public Integer updateCoinCount(String userId, Integer count) {
		return userInfoMapper.updateCoinCount(userId, count);
    }
}