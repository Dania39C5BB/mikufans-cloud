package com.linyajin.mikufans.entity.vo;

import com.linyajin.mikufans.entity.po.UserAction;
import com.linyajin.mikufans.entity.po.VideoComment;
import lombok.Data;

import java.util.List;

@Data
public class VideoCommentResultVO {
    private PaginationResultVO<VideoComment> commentData;
    private List<UserAction> userActionList;
}
