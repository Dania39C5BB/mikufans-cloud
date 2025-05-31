package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.annotation.RecordUserMessage;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.entity.enums.MessageTypeEnum;
import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.UserActionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/userAction")
public class UserActionController extends ABaseController {

    @Resource
    private UserActionService userActionService;

    /**
     * 用户点赞、收藏、投币等操作
     * @param videoId 视频id
     * @param actionType 点赞等类型
     * @param actionCount 投币数量
     * @param commentId 评论id
     * @return ResponseVO
     */
    @PostMapping("/doAction")
    @GlobalInterceptor(checkLogin = true)
    @RecordUserMessage(messageType = MessageTypeEnum.LIKE)
    public ResponseVO doAction(@NotEmpty String videoId,
                               @NotNull Integer actionType ,
                               @Max(2) @Min(1) Integer actionCount,
                               Integer commentId){
        UserAction userAction = new UserAction();
        userAction.setVideoId(videoId);
        userAction.setUserId(getTokenUserInfoDto().getUserId());
        userAction.setActionType(actionType);
        commentId = commentId == null ? Constants.ZERO : commentId;
        userAction.setCommentId(commentId);
        actionCount = actionCount == null ? 1 : actionCount;
        userAction.setActionCount(actionCount);
        userActionService.saveUserAction(userAction);
        return   getSuccessResponseVO(null);
    }

}
