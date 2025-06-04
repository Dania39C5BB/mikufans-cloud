package com.linyajin.mikufans.controller;


import com.linyajin.mikufans.annotation.GlobalInterceptor;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.entity.po.StatisticsInfo;
import com.linyajin.mikufans.entity.query.StatisticsInfoQuery;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.service.StatisticsInfoService;
import com.linyajin.mikufans.utils.DateUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
@Validated
public class UcenterStatisticsController extends ABaseController {

    @Resource
    private StatisticsInfoService statisticsInfoService;


    /**
     * 获取昨天和全部的统计信息
     * @return ResponseVO
     */
    @GetMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getActualTimeStatisticsInfo() {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        String beforeDayDate = DateUtil.getBeforeDayDate(1);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        statisticsInfoQuery.setStatisticsDate(beforeDayDate);

        List<StatisticsInfo> beforeDayDateData = statisticsInfoService.findListByParam(statisticsInfoQuery);

        //获取数据之后根据类型转换成Map
        Map<Integer, Integer> beforeDateDataMap = beforeDayDateData.stream().collect(Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount,
                (item1, item2) -> item2));


        //获取总的统计信息
        Map<String, Integer> totalStatisticsInfo = statisticsInfoService.selectAllStatisticsInfo(tokenUserInfoDto.getUserId());

        //返回的数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("beforeDayDateData", beforeDateDataMap);
        resultMap.put("totalStatisticsInfo", totalStatisticsInfo);

        return getSuccessResponseVO(resultMap);
    }


    /**
     * 获取近7天的统计信息
     * @return ResponseVO
     */
    @GetMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getWeekStatisticsInfo(@NotNull Integer dateType) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();


        List<String> dateList = DateUtil.getBeforeDays(7);


        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setDataType(dateType);
        statisticsInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        statisticsInfoQuery.setStatisticsStartDate(dateList.get(0));
        statisticsInfoQuery.setStatisticsEndDate(dateList.get(dateList.size() - 1));

        List<StatisticsInfo> dataList = statisticsInfoService.findListByParam(statisticsInfoQuery);

        //转换成map集合，方便后续操作
        Map<String, StatisticsInfo> dataMap = dataList.stream().collect(Collectors.toMap(StatisticsInfo::getStatisticsDate, Function.identity(),(item1, item2) -> item2));

        List<StatisticsInfo> resultList = new ArrayList<>();

        //如果根据日期找不到数据，则创建一个新的对象，并且统计数量为0
        for(String date : dateList) {
            StatisticsInfo dateItem = dataMap.get(date);
            if (dateItem == null) {
                //如果数据不存在，则创建一个新的对象
                dateItem = new StatisticsInfo();
                dateItem.setStatisticsDate(date);
                dateItem.setStatisticsCount(0);
                dateItem.setDataType(dateType);
            }
            resultList.add(dateItem);
        }



        return getSuccessResponseVO(resultList);
    }
}
