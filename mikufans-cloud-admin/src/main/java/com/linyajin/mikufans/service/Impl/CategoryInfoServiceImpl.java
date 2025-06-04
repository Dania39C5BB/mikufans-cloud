package com.linyajin.mikufans.service.Impl;

import java.util.ArrayList;
import java.util.List;

import com.linyajin.mikufans.api.consumer.InteractClient;
import com.linyajin.mikufans.api.consumer.WebClient;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.exception.BusinessException;
//import com.linyajin.mikufans.mappers.VideoInfoMapper;
import com.linyajin.mikufans.redis.RedisComponent;
//import com.linyajin.mikufans.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.query.CategoryInfoQuery;
import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.mappers.CategoryInfoMapper;
import com.linyajin.mikufans.service.CategoryInfoService;
import com.linyajin.mikufans.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("categoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {

	@Resource
	private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private WebClient webClient;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CategoryInfo> findListByParam(CategoryInfoQuery param) {
		List<CategoryInfo> categoryInfos = this.categoryInfoMapper.selectList(param);
		if (param.getConvertTree() != null && param.getConvertTree()) {
			categoryInfos = converTree(categoryInfos, Constants.ZERO);
		}
		return categoryInfos;
	}

	//转换树形结构数据
	public List<CategoryInfo> converTree(List<CategoryInfo> dataList , Integer pid){
		List<CategoryInfo> children = new ArrayList<>();

		for (CategoryInfo m : dataList) {
			if (m.getPCategoryId()!=null && m.getCategoryId()!=null && m.getPCategoryId().equals(pid)) {
				m.setChildren(converTree(dataList, m.getCategoryId()));
				children.add(m);
			}
		}
		return children;
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CategoryInfo bean) {
		return this.categoryInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CategoryInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CategoryInfo bean, CategoryInfoQuery param) {
		StringTools.checkParam(param);
		return this.categoryInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CategoryInfoQuery param) {
		StringTools.checkParam(param);
		return this.categoryInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据CategoryId获取对象
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryId(Integer categoryId) {
		return this.categoryInfoMapper.selectByCategoryId(categoryId);
	}

	/**
	 * 根据CategoryId修改
	 */
	@Override
	public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
		return this.categoryInfoMapper.updateByCategoryId(bean, categoryId);
	}

	/**
	 * 根据CategoryId删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryId(Integer categoryId) {
		return this.categoryInfoMapper.deleteByCategoryId(categoryId);
	}

	/**
	 * 根据CategoryCode获取对象
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.selectByCategoryCode(categoryCode);
	}

	/**
	 * 根据CategoryCode修改
	 */
	@Override
	public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
		return this.categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
	}

	/**
	 * 根据CategoryCode删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.deleteByCategoryCode(categoryCode);
	}

	//保存分类或者修改分类
	@Override
	public void saveCategory(CategoryInfo categoryInfo) {
		//根据分类编号查询数据库是否存在该分类
		CategoryInfo dbCategoryInfo = categoryInfoMapper.selectByCategoryCode(categoryInfo.getCategoryCode());

		//如果是新增和修改的情况下，判断分类编号是否存在
		// dbCategoryInfo != null && categoryInfo.getCategoryId()==null该条件代表新增
		//!categoryInfo.getCategoryId().equals(dbCategoryInfo.getCategoryId())
		//如果前端传入的分类ID 跟我数据库查出的Code对应的ID不一致，则抛出异常
		if (dbCategoryInfo != null && categoryInfo.getCategoryId()==null ||
				categoryInfo.getCategoryId()!=null && dbCategoryInfo != null &&
				!categoryInfo.getCategoryId().equals(dbCategoryInfo.getCategoryId())) {
			throw new BusinessException("该分类编号已存在");
		}

		//新增
		if (categoryInfo.getCategoryId() == null) {
			//根据父级分类ID查询最大排序值，然后排序值+1，作为当前分类的排序值
			Integer maxSort = categoryInfoMapper.selectMaxSort(categoryInfo.getPCategoryId());
			categoryInfo.setSort(maxSort + 1);
			categoryInfoMapper.insert(categoryInfo);
		} else {
			//修改
			categoryInfoMapper.updateByCategoryId(categoryInfo, categoryInfo.getCategoryId());
		}

		saveRedisCache();
	}

	//删除分类
	@Override
	public void delCategory(Integer categoryId) {
		//TODO 查询分类下是否有视频并且有二级分类，如果有视频则不允许删除
		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		videoInfoQuery.setCategoryIdOrPCategoryId(categoryId);
		//Integer count = videoInfoService.findCountByParam(videoInfoQuery);
		//TODO 微服务调用
		Integer count = webClient.getVideoCount(videoInfoQuery);
		if (count>0) {
			throw new BusinessException("该分类下有视频，不允许删除");
		}
		CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
		categoryInfoQuery.setCategoryOrPCategoryId(categoryId);
		categoryInfoMapper.deleteByParam(categoryInfoQuery);
		// 刷新缓存
		saveRedisCache();
	}

	//排序分类
	@Override
	public void sortCategory(Integer pCategoryId, String categoryIds) {
		String[] Ids = categoryIds.split(",");
		List<CategoryInfo> categoryInfoList = new ArrayList<>();

		// 初始化排序序号，从0开始
		Integer sort = 0;
		for (String id : Ids) {
			CategoryInfo categoryInfo = new CategoryInfo();
			categoryInfo.setCategoryId(Integer.valueOf(id));
			categoryInfo.setPCategoryId(pCategoryId);
			categoryInfo.setSort(++sort);
			categoryInfoList.add(categoryInfo);
		}

		categoryInfoMapper.updateSortCategory(categoryInfoList);
		saveRedisCache();
	}

	//添加缓存
	public void saveRedisCache() {
		CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
		categoryInfoQuery.setOrderBy("sort asc");
		categoryInfoQuery.setConvertTree(true);
		List<CategoryInfo> categoryInfoList = this.findListByParam(categoryInfoQuery);

		//把数据添加到redis里面
		redisComponent.saveCategoryInfoList(categoryInfoList);
	}

	//获取全部分类列表
	@Override
	public List<CategoryInfo> getAllCateoryList() {
		List<CategoryInfo> categoryInfoList = redisComponent.getCategoryInfoList();
		if (categoryInfoList == null || categoryInfoList.isEmpty()) {
			saveRedisCache();
		}
		return redisComponent.getCategoryInfoList();
	}
}