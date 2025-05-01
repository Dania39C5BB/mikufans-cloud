package com.linyajin.mikufans.entity.query;


import lombok.Data;

/**
 * 参数
 */
@Data
public class CategoryInfoQuery extends BaseParam {


	/**
	 * 自增分类ID
	 */
	private Integer categoryId;

	/**
	 * 分类编码
	 */
	private String categoryCode;

	private String categoryCodeFuzzy;

	/**
	 * 分类名称
	 */
	private String categoryIdName;

	private String categoryIdNameFuzzy;

	private Integer categoryOrPCategoryId;

	private Boolean convertTree;

	/**
	 * 父级分类ID
	 */
	private Integer pCategoryId;

	/**
	 * 图标
	 */
	private String icon;

	private String iconFuzzy;

	/**
	 * 背景图
	 */
	private String background;

	private String backgroundFuzzy;

	/**
	 * 排序号
	 */
	private Integer sort;


}
