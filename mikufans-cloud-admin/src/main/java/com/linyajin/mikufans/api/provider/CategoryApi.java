package com.linyajin.mikufans.api.provider;


import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.service.CategoryInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/innerApi")
public class CategoryApi {

    @Resource
    private CategoryInfoService categoryInfoService;

    @GetMapping("/loadAllCategory")
    public List<CategoryInfo> loadAllCategory() {
        return categoryInfoService.getAllCateoryList();
    }
}
