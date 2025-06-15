package com.linyajin.mikufans.task;

import com.linyajin.mikufans.component.EsSearchComponent;
import com.linyajin.mikufans.dto.VideoPlayInfoDto;
import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoPlayHistory;
import com.linyajin.mikufans.entity.query.VideoPlayHistoryQuery;
import com.linyajin.mikufans.mappers.VideoPlayHistoryMapper;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.service.VideoInfoPostService;
import com.linyajin.mikufans.service.VideoInfoService;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecuteQueueTask {
    private static final Logger log = LoggerFactory.getLogger(ExecuteQueueTask.class);
    //创建线程池 执行任务队列
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private VideoPlayHistoryMapper<VideoPlayHistory , VideoPlayHistoryQuery> videoPlayHistoryMapper;

    @PostConstruct
    public void consumVideoPlayQueue() {
        //启动线程池执行任务队列中的任务
        executorService.execute(() -> {
            //执行任务
            while (true) {
                //从任务队列中取出任务执行
                try {
                    VideoPlayInfoDto videoPlayInfoDto =  redisComponent.getVideoPlayInfo();
                    if (videoPlayInfoDto == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    //更新播放数
                    videoInfoService.addReadCount(videoPlayInfoDto.getVideoId());

                    if (!StringTools.isEmpty(videoPlayInfoDto.getUserId())) {
                        //TODO 记录播放历史
                        VideoPlayHistory videoPlayHistory = new VideoPlayHistory();
                        videoPlayHistory.setVideoId(videoPlayInfoDto.getVideoId());
                        videoPlayHistory.setUserId(videoPlayInfoDto.getUserId());
                        videoPlayHistory.setFileIndex(videoPlayInfoDto.getFileIndex());
                        videoPlayHistory.setLastUpdateTime(new Date());
                        videoPlayHistoryMapper.insertOrUpdate(videoPlayHistory);
                    }

                    //按天记录视频播放数量
                    redisComponent.recordVideoPlayCount(videoPlayInfoDto.getVideoId());

                    //更新es播放数量
                    esSearchComponent.updateDocCount(videoPlayInfoDto.getVideoId() , SearchOrderTypeEnum.VIDEO_PLAY.getFileId(), 1);
                } catch (Exception e) {
                    log.info("获取视频播放文件队列消息失败：{}" , e.getMessage());
                    log.error("获取视频播放文件队列消息失败" , e);
                }
            }
        });
    }
}
