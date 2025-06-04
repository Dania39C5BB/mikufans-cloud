package com.linyajin.mikufans.api.consumer;

import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.query.UserActionQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mikufans-cloud-interact")
public interface InteractClient {

    @PostMapping("/innerApi/interact/userAction/getUserActionList")
    public List<UserAction> getUserActionList(@RequestBody UserActionQuery userActionQuery);

    //删除评论
    @DeleteMapping("/innerApi/interact/delCommentByVideoId")
    public void delCommentByVideoId(@RequestParam("videoId") String videoId);

    //删除弹幕
    @DeleteMapping("/innerApi/interact/delDanMuByVideoId")
    public void delDanMuByVideoId(@RequestParam("videoId") String videoId);
}
