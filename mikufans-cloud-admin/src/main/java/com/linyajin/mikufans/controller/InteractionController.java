package com.linyajin.mikufans.controller;


//import com.linyajin.mikufans.entity.po.VideoComment;
//import com.linyajin.mikufans.entity.query.VideoCommentQuery;
//import com.linyajin.mikufans.entity.query.VideoDanmuQuery;
//import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.api.consumer.InteractClient;
import com.linyajin.mikufans.entity.vo.ResponseVO;
//import com.linyajin.mikufans.service.VideoCommentService;
//import com.linyajin.mikufans.service.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interaction")
@Validated
public class InteractionController extends ABaseController {

    @Resource
    private InteractClient interactClient;

    /**
     * 获取评论
     * @param pageNo 页码
     * @param videoNameFuzzy 模糊查询视频名
     * @return ResponseVO
     */
    @GetMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, String videoNameFuzzy,Integer pageSize) {
        return getSuccessResponseVO(interactClient.loadComment(pageNo, videoNameFuzzy , pageSize));
    }

    /**
     * 删除评论
     * @param commentId 评论id
     * @return ResponseVO
     */
    @DeleteMapping("/delComment")
    public ResponseVO delComment(Integer commentId) {
        interactClient.delComment(commentId);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取弹幕
     * @param pageNo 页码
     * @param videoNameFuzzy 模糊查询视频名
     * @return ResponseVO
     */
    @GetMapping("/loadDanMu")
    public ResponseVO loadDanMu(Integer pageNo ,  String videoNameFuzzy) {
        return getSuccessResponseVO(interactClient.loadDanMu(pageNo ,videoNameFuzzy));
    }

    /**
     * 删除弹幕
     * @param danMuId 弹幕id
     */
    @DeleteMapping("/delDanMu")
    public ResponseVO loadDanMu(@NotNull Integer danMuId) {
        interactClient.delDanMu(danMuId);
        return getSuccessResponseVO(null);
    }
}
