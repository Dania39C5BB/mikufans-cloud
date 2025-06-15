package com.linyajin.mikufans.task;

import com.linyajin.mikufans.service.StatisticsInfoService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SysTask {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    //表示 每天凌晨00:00:00执行
    //要使@Scheduled生效，需在Spring配置中启用定时任务：
    //@EnableScheduling // 关键！启用定时任务支持
    @Scheduled(cron = "0 0 0 * * ?")
    public void staticsData() {
        statisticsInfoService.getStaticsData();
    }
}
