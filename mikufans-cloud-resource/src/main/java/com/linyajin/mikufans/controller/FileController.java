package com.linyajin.mikufans.controller;

import com.linyajin.mikufans.api.consumer.VideoInfoClient;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.SysSettingDto;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.UploadingFileDto;
import com.linyajin.mikufans.dto.VideoPlayInfoDto;
import com.linyajin.mikufans.entity.enums.DateTimePatternEnum;
import com.linyajin.mikufans.entity.enums.ResponseCodeEnum;
import com.linyajin.mikufans.entity.po.VideoInfoFile;
import com.linyajin.mikufans.entity.vo.ResponseVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.redis.RedisComponent;
import com.linyajin.mikufans.utils.DateUtil;
import com.linyajin.mikufans.utils.FFmpegUtils;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;

@RestController
@Validated
public class FileController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private FFmpegUtils ffmpegUtils;

//    @Resource
//    private VideoInfoFileServiceImpl videoInfoFileService;

    @Resource
    private VideoInfoClient videoInfoClient;


    //获取资源
    @GetMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotNull String sourceName) throws IOException {
        log.info("获取资源:{}", sourceName);
        //如果路径不合法
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //获取文件后缀
        String fileSuffix = StringTools.getFileSuffix(sourceName);
        if (fileSuffix != null) {
            response.setContentType("image/" + fileSuffix.replace(".", ""));
        }
        response.setHeader("Cache-Control" , "max-age=2592000");

        readFile(response, sourceName);
//        return getSuccessResponseVO(null);

    }

    //读取文件
    public void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_DIR + filePath);
        if (!file.exists()) {
            return;
        }
        try(OutputStream out = response.getOutputStream(); InputStream in = new FileInputStream(file)){
            byte[] byteData = new byte[1024];
            int len = 0;
            //循环读取文件数据到缓冲区，并写入到输出流中
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件失败", e);
        }
    }

    /**
     * 视频预上传
     * @param fileName 文件名
     * @param chunks 总分片数量
     * @return ResponseVO 响应对象
     */
    @PostMapping("/preUploadVideo")
    public ResponseVO preUploadVideo(@NotEmpty String fileName, @NotNull  Integer chunks)  {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        String uploadId = redisComponent.savePreVideoFileInfo(tokenUserInfoDto.getUserId(), fileName, chunks);
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 视频上传
     * @param chunkFile 分片的文件名
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引(从第几片开始上传)
     * @return ResponseVO 响应对象
     */
    @PostMapping("/uploadVideo")
    public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile, @NotEmpty String uploadId, @NotNull Integer chunkIndex) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //拿到预上传的文件信息
        UploadingFileDto fileDto = redisComponent.getPreVideoFileInfo(uploadId, tokenUserInfoDto.getUserId());
        if (fileDto == null) {
            throw new BusinessException("文件信息不存在，请重新上传");
        }

        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        if (fileDto.getFileSize() > sysSettingDto.getVideoSize() * Constants.MB_SIZE) {
            throw new BusinessException("文件大小超过限制");
        }
        fileDto.setFileSize(chunkFile.getSize());
        //判断分片
        // - 当前分片索引不能比已上传的最大索引大 超过1（防止跳过中间分片）
        // - 分片索引不能超过总分片数-1（索引从0开始）
        //chunkIndex - 1 > fileDto.getChunkIndex()
        //确保客户端按顺序上传分片（允许跳过一个分片，但不可跳过多个）。
        //chunkIndex > (fileDto.getChunks() - 1)
        //防止分片索引超过总分片数（例如总分片为10，索引最大为9）。
        if ((chunkIndex - 1) > fileDto.getChunkIndex() || chunkIndex > ( fileDto.getChunks() - 1)) {
            throw new BusinessException("分片索引不正确");
        }

        String filePath = appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_DIR_TEMP + fileDto.getFilePath();
        log.info("文件路径：{}", filePath);
        File targetFile = new File(filePath + "/" + chunkIndex);
         //把每个的分片文件写入到临时文件夹中
        chunkFile.transferTo(targetFile);
        //更新Redis中的上传进度（例如记录已上传的分片索引）
        fileDto.setChunkIndex(chunkIndex);
        redisComponent.updatePreVideoFileInfo(tokenUserInfoDto.getUserId(), fileDto);
        return getSuccessResponseVO(null);

    }

    @DeleteMapping("/delUploadVideo")
    public ResponseVO deleteVideo(@NotEmpty String uploadId) throws IOException {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        UploadingFileDto fileDto = redisComponent.getPreVideoFileInfo(uploadId, tokenUserInfoDto.getUserId());

        if (fileDto == null) {
            throw new BusinessException("文件信息不存在，请重新上传");
        }

        //清除redis里面的记录
        redisComponent.deletePreVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);

        //删除临时文件夹中的文件
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_DIR_TEMP + fileDto.getFilePath());
       //删除目录下的所有文件和目录
        FileUtils.deleteDirectory(file);
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 上传图片
     * @param file 文件
     * @param createIsThumbnail 是否生成缩略图
     * @return ResponseVO 响应对象
     */
    @PostMapping("/uploadImage")
    public ResponseVO uploadImage(@NotNull MultipartFile file, @NotNull Boolean createIsThumbnail) throws IOException {

        return getSuccessResponseVO(uploadImageInner(file , createIsThumbnail));

    }

    public String uploadImageInner(MultipartFile file, Boolean createIsThumbnail) throws IOException {
        //生成年月日
        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());

        //保存文件的目录路径
        String folder = appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_COVER + day;

        File folderPath = new File(folder);
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        //解析文件名
        String originalFilename = file.getOriginalFilename();
        //获取后缀名
        String fileSuffix = StringTools.getFileSuffix(originalFilename);
        //重新命名文件 防止文件重复
        String realFileName = StringTools.getRandomStrUID(20) + fileSuffix;
        //完整的文件路径
        String filePath = folder + "/" + realFileName;
        //转存文件
        file.transferTo(new File(filePath));
        if (createIsThumbnail != null && createIsThumbnail) {
            ffmpegUtils.createImageThumbnail(filePath);
        }
        return Constants.FILE_COVER + day + "/" + realFileName;
    }

    //获取视频的m3u8文件的资源
    @GetMapping("/getVideoResource/{fileId}/")
    public void getVideoResource(HttpServletResponse response , @PathVariable @NotEmpty String fileId)   {
//        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        VideoInfoFile videoInfoFile = videoInfoClient.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + "index.m3u8");
        //TODO 在redis里面更新视频的阅读信息
        VideoPlayInfoDto videoPlayInfoDto = new VideoPlayInfoDto();
        videoPlayInfoDto.setVideoId(videoInfoFile.getVideoId());
        videoPlayInfoDto.setFileIndex(videoInfoFile.getFileIndex());
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoFromCookie();
        if (tokenUserInfoDto != null) {
            videoPlayInfoDto.setUserId(tokenUserInfoDto.getUserId());
        }
        redisComponent.addVideoPlay(videoPlayInfoDto);
    }

    //获取视频的ts资源
    @GetMapping("/getVideoResource/{fileId}/{ts}")
    public void getVideoResourseTs(HttpServletResponse response , @PathVariable @NotEmpty String fileId , @PathVariable @NotEmpty String ts)   {
//        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        VideoInfoFile videoInfoFile = videoInfoClient.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + ts);
    }

}
