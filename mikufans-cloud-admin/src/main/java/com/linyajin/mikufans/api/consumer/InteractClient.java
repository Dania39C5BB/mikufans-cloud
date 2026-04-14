package com.linyajin.mikufans.api.consumer;


import com.linyajin.mikufans.entity.query.VideoInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mikufans-cloud-interact")
public interface InteractClient {


    @GetMapping("/innerApi/interact/admin/loadComment")
    public PaginationResultVO loadComment(@RequestParam("pageNo") Integer pageNo,@RequestParam("videoNameFuzzy")  String videoNameFuzzy,@RequestParam("pageSize") Integer pageSize);


    @DeleteMapping("/innerApi/interact/admin/delComment")
    public void delComment(@RequestParam("commentId") Integer commentId);


    @GetMapping("/innerApi/interact/admin/loadDanMu")
    public PaginationResultVO loadDanMu(@RequestParam("pageNo") Integer pageNo,@RequestParam("videoNameFuzzy")  String videoNameFuzzy);


    @DeleteMapping("/innerApi/interact/admin/delDanMu")
    public void delDanMu(@RequestParam("danMuId") Integer danMuId);

}
