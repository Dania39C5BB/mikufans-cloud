package com.linyajin.mikufans.component;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.UploadingFileDto;
import com.linyajin.mikufans.entity.enums.VideoFileTransferResultEnum;
import com.linyajin.mikufans.entity.enums.VideoStatusEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.entity.po.VideoInfoPost;
import com.linyajin.mikufans.entity.query.VideoInfoFilePostQuery;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.utils.FFmpegUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;

@Component
@Slf4j
public class TransferFileComponent {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig  appConfig;

    @Resource
    private FFmpegUtils ffmpegUtils;

    @Resource
    private VideoInfoClient videoInfoClient;



    //转码文件队列任务
    public void transferVideoFile(VideoInfoFilePost videoInfoFilePost) {
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        try {
            //从redis中拿到上传的分P文件信息
            UploadingFileDto fileDto = redisComponent.getPreVideoFileInfo(videoInfoFilePost.getUploadId(), videoInfoFilePost.getUserId());
            //从临时目录中拿到文件 然后移动到正式目录中
            //临时目录
            String tempFilePath = appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_DIR_TEMP + fileDto.getFilePath();
            File tempFile = new File(tempFilePath);
            //正式目录
            String targetFilePath = appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_VIDEO + fileDto.getFilePath();
            File targetFile = new File(targetFilePath);
            //移动文件到正式目录
            FileUtils.copyDirectory(tempFile , targetFile);
            //删除临时目录
            FileUtils.deleteDirectory(tempFile);
            //删除redis中的临时文件信息
            redisComponent.deletePreVideoFileInfo(videoInfoFilePost.getUserId() ,videoInfoFilePost.getUploadId());
            //合并文件之后要存到的目录
            String completeVideo = targetFilePath + Constants.TEMP_VIDEO_NAME;
            unionFile(targetFilePath , completeVideo , true);
            //获取文件播放时长
            Integer videoDuration = ffmpegUtils.getVideoDuration(completeVideo);
            updateFilePost.setDuration(videoDuration);
            updateFilePost.setFileSize(new File(completeVideo).length());
            updateFilePost.setFilePath(Constants.FILE_VIDEO + fileDto.getFilePath());
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());
            convertVideoTs(completeVideo);
        } catch (Exception e) {
            log.error("转码失败", e);
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
        } finally {
            //调用web服务的资源 更新视频文件信息
            videoInfoClient.transferVideoInfoFile(videoInfoFilePost.getVideoId() , videoInfoFilePost.getUploadId() , videoInfoFilePost.getUserId() , updateFilePost);
        }
    }

    //转成ts文件 实际播放用的是ts文件
    private void convertVideoTs(String completeVideo) {
        File file = new File(completeVideo);
        //拿到上级目录路径
        File parentFile = file.getParentFile();
        //获取编码格式
        String videoCodec = ffmpegUtils.getVideoCodec(completeVideo);
        //如果不是mp4格式 则需要转码
        if (!videoCodec.equals("h264")) {
            String tempFileName = completeVideo + "_temp";
            //尝试将原文件重命名/移动到目标文件
            //因为不能生成一个新文件自己替换自己 所以先将原文件重命名复制出来一个 然后转码之后把源文件替换 删掉复制出来的文件
            new File(completeVideo).renameTo(new File(tempFileName));
            //转换格式
            ffmpegUtils.convertHevcToMp4(tempFileName , completeVideo);
            new File(tempFileName).delete();
        }
        ffmpegUtils.convertVideoToTs(parentFile , completeVideo);
        file.delete();

    }

    //合并文件
    private void unionFile(String dirPath ,String toFilePath , Boolean isDelSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        //获取目录中的文件列表
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        //合并文件
        //使用RandomAccessFile以读写模式打开目标文件
        try(RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            //创建10KB的缓冲区用于读写操作
            byte[] bytes = new byte[1024 * 10];
            for(int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象
                //处理每个分片文件
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                //使用try-with-resources确保writeFile自动关闭
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(bytes)) != -1) {
                        //将每个分片内容写入目标文件
                        writeFile.write(bytes, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            throw new BusinessException("合并文件" + dirPath + "失败");
        } finally {
            //如果isDelSource为true，删除所有分片文件
            if (isDelSource) {
                for(int i = 0; i < fileList.length; i++) {
                    fileList[i].delete();
                }
            }
        }
    }

}
