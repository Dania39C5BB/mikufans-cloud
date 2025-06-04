package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.entity.enums.UserActionTypeEnum;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.query.UserActionQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.UserActionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/uHome")
public class UHomeController extends ABaseController {

    @Resource
    private UserActionService userActionService;


    /**
     * 查询我的收藏列表
     * @param userId 用户id
     * @param pageNo 页码
     * @return ResponseVO 返回结果vo
     */
    @GetMapping("/loadUserCollectList")
    public ResponseVO loadUserCollectList(@NotEmpty String userId ,Integer  pageNo) {

        UserActionQuery userActionQuery = new UserActionQuery();
        userActionQuery.setUserId(userId);
        userActionQuery.setPageNo(pageNo);
        userActionQuery.setOrderBy("action_time desc");
        userActionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        userActionQuery.setQueryVideoInfo(true);
        PaginationResultVO<UserAction> resultVO = userActionService.findListByPage(userActionQuery);


        return getSuccessResponseVO(resultVO);
    }


}
