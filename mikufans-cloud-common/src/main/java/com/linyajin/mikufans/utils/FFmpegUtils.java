package com.linyajin.mikufans.utils;

//import com.linyajin.mikufans.config.ReadyAdminConfig;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;

@Component
public class FFmpegUtils {
    @Resource
    private AppConfig appConfig;

    //创建缩略图
    public void createImageThumbnail(String filePath) {
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        //第二个参数：输入文件路径，第三个参数：输出文件路径+文件后缀
        CMD = String.format(CMD, filePath , filePath.replaceFirst(".jpg", "") + Constants.IMAGE_THUMBNAIL_SUFFIX);
        ProcessUtils.executeCommand(CMD, false);
    }

    //获取视频的时长
    public Integer getVideoDuration(String filePath) {
        String CMD = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        CMD = String.format(CMD, filePath);
        String res = ProcessUtils.executeCommand(CMD, false);
        if (StringTools.isEmpty(res)) {
            return 0;
        }
        res = res.replace("\n", "");

        return new BigDecimal(res).intValue(); //返回时长，单位：秒
    }

    //获取文件的编码格式
    public String getVideoCodec(String filePath) {
        String CMD = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        CMD = String.format(CMD, filePath);
        String res = ProcessUtils.executeCommand(CMD, false);
        //从 String str = "[STREAM]\n" +
        //                  "codec_name=h264\n" +
        //                  "[/STREAM]";
        //截取出好h264的编码格式，去掉前面的"[STREAM]\ncodec_name="和后面的"[/STREAM]"
        res = res.replace("\n", "");
        //第一步结果：[STREAM]codec_name=h264[/STREAM]
        res = res.substring(res.indexOf("=") + 1);
        //第二步结果：h264[/STREAM]
        String codecName = res.substring(0,res.indexOf("["));
        //第三步结果：h264
        return codecName;
    }

    //转换格式
    public void convertHevcToMp4(String tempFileName, String filePath) {
        String CMD = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        //转换之后的文件名由filePath这个变量决定，也就是原来的文件名
        CMD = String.format(CMD, tempFileName, filePath);
        ProcessUtils.executeCommand(CMD, false);
    }

    //转换视频为ts格式
    public void convertVideoToTs(File tsFolder, String VideoPath) {
        String CMD_TO_TS = "ffmpeg -y -i \"%s\" -vcodec copy -acodec copy -bsf:v h264_mp4toannexb \"%s\"";
        String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";
        String tsPath = tsFolder + "/" + "index.ts";
        //生成ts文件
        String cmdTs = String.format(CMD_TO_TS, VideoPath , tsPath);
        ProcessUtils.executeCommand(cmdTs, false);
        //生成索引文件.m3u8文件和切片.ts文件
        // 参数1: 输入TS文件路径
        // 参数2: 输出m3u8索引文件路径
        // 参数3: 输出TS分片的目录路径
        cmdTs = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/index.m3u8", tsFolder.getPath());
        ProcessUtils.executeCommand(cmdTs, false);
        //删除临时ts文件
        new File(tsPath).delete();
    }
}
