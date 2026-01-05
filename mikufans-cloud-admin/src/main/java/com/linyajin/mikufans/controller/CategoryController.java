package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.dto.CategoryInfoAdminDto;
import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.entity.query.CategoryInfoQuery;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.CategoryInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController {

    @Resource
    private CategoryInfoService categoryInfoService;

    @PostMapping("/loadCategory")
    public ResponseVO loadCategory(@RequestBody CategoryInfoQuery query) {
        query.setOrderBy("sort asc");
        query.setConvertTree(true);
        List<CategoryInfo> list = categoryInfoService.findListByParam(query);
        return getSuccessResponseVO(list);
    }

    //保存分类和修改分类
    @PostMapping("/saveCategory")
    public ResponseVO saveCategory(@RequestBody @Valid CategoryInfoAdminDto categoryInfoAdminDto) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setPCategoryId(categoryInfoAdminDto.getPCategoryId());
        categoryInfo.setCategoryId(categoryInfoAdminDto.getCategoryId());
        categoryInfo.setCategoryCode(categoryInfoAdminDto.getCategoryCode());
        categoryInfo.setCategoryIdName(categoryInfoAdminDto.getCategoryName());
        categoryInfo.setIcon(categoryInfoAdminDto.getIcon());
        categoryInfo.setBackground(categoryInfoAdminDto.getBackground());

         categoryInfoService.saveCategory(categoryInfo);

        return getSuccessResponseVO(null);
    }

    //删除分类
    @DeleteMapping("/delCategory")
    public ResponseVO delCategory(@NotNull Integer categoryId) {
        categoryInfoService.delCategory(categoryId);
        return getSuccessResponseVO(null);
    }

    //排序
    @PostMapping("/sortCategory")
    public ResponseVO sortCategory(@NotNull Integer pCategoryId , @NotEmpty String categoryIds) {
        categoryInfoService.sortCategory(pCategoryId , categoryIds);
        return getSuccessResponseVO(null);
    }
}
