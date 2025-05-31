package com.linyajin.mikufans.api.consumer;

import com.linyajin.mikufans.entity.po.CategoryInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "mikufans-cloud-admin")
public interface CategoryClient {

    @GetMapping("/innerApi/loadAllCategory")
    List<CategoryInfo> loadAllCategory();
}
