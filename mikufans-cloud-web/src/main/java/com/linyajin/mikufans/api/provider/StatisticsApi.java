package com.linyajin.mikufans.api.provider;


import com.linyajin.mikufans.entity.enums.StatisticsTypeEnum;
import com.linyajin.mikufans.entity.po.StatisticsInfo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.StatisticsInfoQuery;
import com.linyajin.mikufans.entity.query.UserInfoQuery;
import com.linyajin.mikufans.service.StatisticsInfoService;
import com.linyajin.mikufans.service.UserInfoService;
import com.linyajin.mikufans.utils.DateUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/innerApi/statistics/admin")
@Validated
@Slf4j
public class StatisticsApi {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private UserInfoService userInfoService;


    //获取昨天和全部的统计信息(管理端)
    @GetMapping("/getActualTimeStatisticsInfo")
    public Map getActualTimeStatisticsInfo() {
        String beforeDayDate = DateUtil.getBeforeDayDate(2);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setStatisticsDate(beforeDayDate);

        //获取前一天的数据
        List<StatisticsInfo> beforeDayDateData = statisticsInfoService.findTotalStatisticsInfo(statisticsInfoQuery);

        //因为是管理端所以需要把粉丝数量改成用户数量
        Integer userCount = userInfoService.findCountByParam(new UserInfoQuery());
        beforeDayDateData.forEach(item -> {
            if (StatisticsTypeEnum.FANS.getType().equals(item.getDataType())) {
                item.setStatisticsCount(userCount);
            }
        });


        //获取数据之后根据类型转换成Map
        Map<Integer, Integer> beforeDateDataMap = beforeDayDateData.stream().collect(Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount,
                (item1, item2) -> item1+item2));


        //获取总的统计信息
        Map<String, Integer> totalStatisticsInfo = statisticsInfoService.selectAllStatisticsInfo(null);

        //返回的数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("beforeDayDateData", beforeDateDataMap);
        resultMap.put("totalStatisticsInfo", totalStatisticsInfo);

        return resultMap;
    }




    //获取近7天的统计信息
    @GetMapping("/getWeekStatisticsInfo")
    public List<StatisticsInfo> getWeekStatisticsInfo(Integer dateType) {

        log.info("获取近7天的统计信息:{}" , dateType);
        List<String> dateList = DateUtil.getBeforeDays(7);


        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setDataType(dateType);
        statisticsInfoQuery.setStatisticsStartDate(dateList.get(0));
        statisticsInfoQuery.setStatisticsEndDate(dateList.get(dateList.size() - 1));
        statisticsInfoQuery.setOrderBy("statistics_date asc");
        List<StatisticsInfo> dataList = null;

        //因为是管理端需要查所有系统的
        //所以在管理端查询用户数量的时候  前端传来的 dateType 是粉丝的类型
        //在判断的时候需要判断一下，如果是粉丝的类型则需要换成用户数量
        if (!StatisticsTypeEnum.FANS.getType().equals(dateType)){
            dataList = statisticsInfoService.findTotalStatisticsInfo(statisticsInfoQuery);
        } else {
            dataList = statisticsInfoService.findUserCountByParam(statisticsInfoQuery);
        }


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

        return resultList;
    }
}
