package com.linyajin.mikufans.task;

import com.linyajin.mikufans.component.TransferFileComponent;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.redis.RedisComponent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private TransferFileComponent transferFileComponent;

    /**
     * 转码文件任务队列
     */
    @PostConstruct
    public void consumTransferFileQueue() {
        //启动线程池执行任务队列中的任务
        executorService.execute(() -> {
            //执行任务
            while (true) {
                //从任务队列中取出任务执行
                try {
                    VideoInfoFilePost videoInfoFilePost =  redisComponent.getFileFromTransferQueue();
//                    videoInfoFilePost 如果为空 则代表里面没有任务 休眠这个线程一段时间后再执行
                    if (videoInfoFilePost == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    transferFileComponent.transferVideoFile(videoInfoFilePost);
                } catch (Exception e) {
                    log.info("获取转码文件队列失败：{}" , e);
                    e.printStackTrace();
                }
            }
        });
    }
}
