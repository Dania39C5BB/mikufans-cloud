package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.UserMessageCountDto;
import com.linyajin.mikufans.entity.enums.MessageReadTypeEnum;
import com.linyajin.mikufans.entity.po.UserMessage;
import com.linyajin.mikufans.entity.query.UserMessageQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.UserMessageService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@Validated
public class UserMessageController extends ABaseController {

    @Resource
    private UserMessageService userMessageService;

    /**
     * 获取未读消息数量
     * @return ResponseVO
     */
    @GetMapping("/getNoReadCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCount() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());

        Integer count = userMessageService.findCountByParam(userMessageQuery);
        return getSuccessResponseVO(count);
    }

    /**
     * 获取未读消息数量（分组）
     * @return ResponseVO
     */
    @GetMapping("/getNoReadCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCountGroup() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<UserMessageCountDto> dataList = userMessageService.getMessageNoReadCount(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(dataList);
    }


    /**
     * 读取消息（全部）
     * @param messageType 要读取的消息类型
     * @return ResponseVO
     */
    @PutMapping("/readAllMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO readAllMessage(@NotNull Integer messageType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        //要更新的数据
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(tokenUserInfoDto.getUserId());
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        //更新条件
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setMessageType(messageType);
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());

        userMessageService.updateByParam(userMessage, userMessageQuery);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取消息列表（分页）
     * @param messageType 消息类型
     * @param pageNo 页码
     * @return ResponseVO
     */
    @GetMapping("/loadMessageList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMessageList(@NotNull Integer messageType , @NotNull Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setMessageType(messageType);
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setOrderBy("message_id desc");

        PaginationResultVO<UserMessage> resultVO = userMessageService.findListByPage(userMessageQuery);
        return getSuccessResponseVO(resultVO);
    }


    /**
     * 删除消息
     * @param messageId 消息ID
     * @return ResponseVO
     */
    @DeleteMapping("/delMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMessageList(@NotNull Integer messageId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setMessageId(messageId);
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageService.deleteByParam(userMessageQuery);
        return getSuccessResponseVO(null);
    }

}
