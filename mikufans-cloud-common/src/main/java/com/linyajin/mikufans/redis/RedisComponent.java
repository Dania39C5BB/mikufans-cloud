package com.linyajin.mikufans.redis;



import com.linyajin.mikufans.config.AppConfig;
//import com.linyajin.mikufans.config.ReadyAdminConfig;
//import com.linyajin.mikufans.config.ReadyWebConfig;
import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.dto.SysSettingDto;
import com.linyajin.mikufans.dto.TokenUserInfoDto;
import com.linyajin.mikufans.dto.UploadingFileDto;
import com.linyajin.mikufans.dto.VideoPlayInfoDto;
import com.linyajin.mikufans.entity.enums.DateTimePatternEnum;
import com.linyajin.mikufans.entity.po.CategoryInfo;
import com.linyajin.mikufans.entity.po.VideoInfoFilePost;
import com.linyajin.mikufans.utils.DateUtil;
import com.linyajin.mikufans.utils.JwtUtil;
import com.linyajin.mikufans.utils.StringTools;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component("redisComponent")
public class RedisComponent {

    private static final Logger log = LoggerFactory.getLogger(RedisComponent.class);

    @Resource
    private AppConfig appConfig;

//    @Resource
//    private ReadyAdminConfig readyAdminConfig;

    @Resource
    private RedisUtils redisUtils;

    public void saveTokenUserInfo(TokenUserInfoDto tokenUserInfoDto , HashMap<String, Object> claims)  {

        tokenUserInfoDto.setExpireTime(System.currentTimeMillis() + (appConfig.getWebJwtExpiresIn() * 7L));
        //生成token
        String token = JwtUtil.createJWT(appConfig.getWebJwtSecret(), appConfig.getWebJwtExpiresIn() * 7L, claims);
        tokenUserInfoDto.setToken(token);

        Claims decoded = JwtUtil.parseJWT(appConfig.getWebJwtSecret(), token);

        System.out.println("解码结果: " + decoded);
        System.out.println("解码结果: " + decoded.getExpiration().getTime());
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB+token,tokenUserInfoDto, appConfig.getWebJwtExpiresIn() * 7L);

    }

    //更新redis中的用户信息
    public void updateTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB+tokenUserInfoDto.getToken(),tokenUserInfoDto, appConfig.getWebJwtExpiresIn() * 7L);
    }

    //获取token用户信息
    public TokenUserInfoDto getTokenUserInfo(String token) {
      return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB+token);
    }

    //重新登录删除token
    public void deleteToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB+token);
    }


    // ==========================Admin==========================
    //保存Admin的token信息
    public String saveTokenInfoAdmin(String account , HashMap<String, Object> claims) {
        //生成token
        String token = JwtUtil.createJWT(appConfig.getAdminJwtSecret(), appConfig.getAdminJwtExpiresIn(), claims);

        Claims decoded = JwtUtil.parseJWT(appConfig.getAdminJwtSecret(), token);

        System.out.println("解码结果: " + decoded);
        System.out.println("解码结果: " + decoded.getExpiration().getTime());
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN+token,account+token, appConfig.getAdminJwtExpiresIn());

        return token;
    }

    //重新登录删除token
    public void deleteAdminToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN+token);
    }

    public Object getTokenInfo(String token) {
        return  redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN+token);
    }

    //在redis中保存分类信息列表
    public void saveCategoryInfoList(List<CategoryInfo> categoryInfoList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST,categoryInfoList);
    }

    //获取redis中分类信息列表
    public List<CategoryInfo> getCategoryInfoList() {
       return (List<CategoryInfo>)redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    //保存预览视频文件信息到redis
    public String savePreVideoFileInfo(String userId , String fileName, Integer chunks) {
        //设置上传id，随机生成15位字符串
        String uploadId = StringTools.getRandomStrUID(15);
        UploadingFileDto fileDto = new UploadingFileDto();
        fileDto.setFileName(fileName);
        fileDto.setChunks(chunks);
        fileDto.setUploadId(uploadId);
        fileDto.setChunkIndex(0);
        //设置目录
        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());

        //拼接文件路径
        String filePath = day + "/" + userId + uploadId;

        //存放到临时目录下
        String folder = appConfig.getProjectFolder() + Constants.FILE_DIR + Constants.FILE_DIR_TEMP + filePath;

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        fileDto.setFilePath(filePath);

        //存入redis中，设置过期时间一天
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, fileDto , Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return uploadId;
    }

    //获取redis中预上传的视频文件信息
    public UploadingFileDto getPreVideoFileInfo(String uploadId , String userId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }


    //获取设置信息
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        //如果redis中没有设置信息，则返回默认值
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }
    //更新系统设置信息
    public void saveSysSettingDto(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING ,sysSettingDto);
    }

    //更新redis中的视频文件信息
    public void updatePreVideoFileInfo(String userId, UploadingFileDto fileDto) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + fileDto.getUploadId(), fileDto , Constants.REDIS_KEY_EXPIRES_ONE_DAY);

    }

    //清除redis中的视频文件信息
    public void deletePreVideoFileInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    //保存要删除的文件列表到redis中
    public void saveDeleteFileList(List<String> filePathList , String videoId) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL + videoId, filePathList, Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7L);
    }

    //获取要删除的视频文件列表
    public List<String> getDelFileList(String videoId) {
        return redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    //清除redis中要删除文件的列表
    public void cleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    //保存要添加的文件列表到redis中
    public void saveAddFileList(List<VideoInfoFilePost> addFileList) {
        //往左边添加元素
        redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER,  addFileList , 0);
    }

    public VideoInfoFilePost getFileFromTransferQueue() {
        //从右边 弹出元素，并返回该元素
        return (VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
    }

    //记录在线播放人数
    public Integer reportVideoPlayOneLine(String fileId , String deviceId) {
        // 生成用户播放记录key
        String userPlayOneLineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER , fileId,deviceId);
        //视频分P在线播放人数数量
        String playOneLineCountKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE , fileId);

        //如果没有当前key
        if (!redisUtils.keyExists(userPlayOneLineKey)) {
            redisUtils.setex(userPlayOneLineKey,fileId, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 8);
            //增加在线人数计数器，并设置10秒过期时间(10秒无更新则重置)
            // 返回增加后的在线人数
            return redisUtils.incrementex(playOneLineCountKey,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 10).intValue();
        }
        redisUtils.expire(playOneLineCountKey,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 10);
        redisUtils.expire(userPlayOneLineKey, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS  * 8);
        Integer count = (Integer) redisUtils.get(playOneLineCountKey);
        return count == null ? 1 : count;
    }

    //减少在线播放人数
    public void decrementVideoPlayCount(String key) {
        log.info("=====减少在线播放人数=====: " + key);
        redisUtils.decrement(key);
    }

    //添加搜索热词
    public void addKeyWordCount(String keyword) {
        String todayKey = Constants.REDIS_KEY_VIDEO_SEARCH_COUNT + LocalDate.now(); // 例如: video:search:count:2023-08-01
        redisUtils.zaddCount(todayKey,keyword);
        // 设置键的过期时间（1天后自动删除）
        redisUtils.expire(todayKey, 24*60*60*1000L);
    }

    //获取热词
    /**
     * 获取搜索量最高的前N个热词
     * @param top 返回的热词数量(索引从0开始 所以需要 -1)
     * @return 热词列表（从高到低排序）
     */
    public List<String> getKeyWordTop(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT,top-1);
    }

    /**
     * 获取某天的前N个热词（按搜索量降序）
     * @param date 日期，格式 yyyy-MM-dd（如 "2023-08-01"）
     * @param topN 返回的热词数量(索引从0开始 所以需要 -1)
     * @return 热词列表（从高到低排序）
     */
    public List<String> getDailyTopKeywords(LocalDate date, Integer topN) {
        String todayKey = Constants.REDIS_KEY_VIDEO_SEARCH_COUNT + date; // 例如: video:search:count:2023-08-01
        // ZREVRANGE: 降序获取 [0, topN-1] 范围的数据
        return redisUtils.getZSetList(todayKey, topN - 1);
    }

    //用户播放视频的历史记录
    public void addVideoPlay(VideoPlayInfoDto videoPlayInfoDto) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY ,videoPlayInfoDto,null );
    }

    //用户播放视频的历史记录
    //从名为 Constants.REDIS_KEY_QUEUE_VIDEO_PLAY 的 Redis 列表中弹出最右边的一个元素
    public VideoPlayInfoDto getVideoPlayInfo() {
        return (VideoPlayInfoDto) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY );
    }

    //按天记录视频播放数量
    public void recordVideoPlayCount(String videoId) {
        String date = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());

        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date + ":" + videoId, Constants.REDIS_KEY_EXPIRES_ONE_DAY * 2L);
    }

    //从Redis批量获取视频播放量数据
    public Map<String, Integer> getVideoPlayCount(String date) {
        Map<String,Integer> result = redisUtils.getBatch(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date);
        return result;
    }
}
