package com.linyajin.mikufans.controller;



import com.linyajin.mikufans.api.consumer.WebClient;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/statistics")
@Validated
@Slf4j
public class UcenterAdminStatisticsController extends ABaseController {

    @Resource
    private WebClient webClient;

    /**
     * 获取昨天和全部的统计信息(管理端)
     * @return ResponseVO
     */
    @GetMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo() {
        return getSuccessResponseVO(webClient.getActualTimeStatisticsInfo());
    }


    /**
     * 获取近7天的统计信息
     * @return ResponseVO
     */
    @GetMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(@NotNull Integer dateType) {
        return getSuccessResponseVO(webClient.getWeekStatisticsInfo(dateType));
    }
}
