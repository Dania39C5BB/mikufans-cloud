package com.linyajin.mikufans.constants;

public class Constants {
    public static final String REDIS_KEY_PREEFIX = "mikuFans:";

    public static  String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREEFIX+"checkCode:";

    public static final Integer THEME_ONE = 1;

    public static final Integer THEME_ZERO = 0;

    public static final Long MB_SIZE = 1024*1024L;

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer HOUR_24 = 24;

    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREEFIX+"token:web:";

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREEFIX+"token:admin:";


    public static final String TOKEN_WEB = "token";

    public static final String TOKEN_ADMIN = "adminToken";

    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60000;

    public static final Integer REDIS_KEY_EXPIRES_ONE_SECONDS = 1000;

    public static final Integer REDIS_KEY_EXPIRES_ONE_DAY = REDIS_KEY_EXPIRES_ONE_MIN*60*24;

    public static final String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREEFIX + "category:list:";

    public static final String FILE_DIR = "file/";

    public static final String FILE_COVER = "cover/";

    public static final String FILE_VIDEO = "video/";

    public static final String FILE_DIR_TEMP = "temp/";

    public static final String TEMP_VIDEO_NAME = "/temp.mp4";

    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail.jpg";

    public static final String REDIS_KEY_UPLOADING_FILE = REDIS_KEY_PREEFIX + "uploading:";

    public static final String REDIS_KEY_SYS_SETTING = REDIS_KEY_PREEFIX + "sysSetting:";

    public static final String REDIS_KEY_FILE_DEL = REDIS_KEY_PREEFIX + "file:list:del:";

    public static final String REDIS_KEY_QUEUE_TRANSFER = REDIS_KEY_PREEFIX + "queue:transfer:";

    //更新昵称所需的数量
    public static final Integer UPDATE_NICKNAME_COIN_COUNT = 6;

    //视频在线
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX = REDIS_KEY_PREEFIX + "video:play:online:";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + "count:%s";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX = "user:";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX +  "%s:%s";


    //视频搜索热词
    public static final String REDIS_KEY_VIDEO_SEARCH_COUNT =  REDIS_KEY_PREEFIX + "video:search:count:";
    //用户的视频播放记录
    public static final String REDIS_KEY_QUEUE_VIDEO_PLAY =  REDIS_KEY_PREEFIX + "queue:video:play:";
    //按天记录视频播放的数量
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT =  REDIS_KEY_PREEFIX + "video:playCount:";

}
