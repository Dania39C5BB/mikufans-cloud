package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.api.consumer.CategoryClient;
import com.linyajin.mikufans.dto.CategoryInfoAdminDto;
import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController extends ABaseController {

    @Resource
    private CategoryClient categoryClient;

    @GetMapping("/loadAllCategory")
    public ResponseVO loadAllCategory() {
        return getSuccessResponseVO(categoryClient.loadAllCategory());
    }
}
