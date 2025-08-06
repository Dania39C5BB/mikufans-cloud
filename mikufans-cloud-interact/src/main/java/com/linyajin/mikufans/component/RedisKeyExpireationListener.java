package com.linyajin.mikufans.component;

import com.linyajin.mikufans.constants.Constants;
import com.linyajin.mikufans.redis.RedisComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 监听redis key过期事件
 */

@Component
@Slf4j
public class RedisKeyExpireationListener extends KeyExpirationEventMessageListener {

    @Resource
    private RedisComponent redisComponent;

    public RedisKeyExpireationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        //如果不是以某个前缀开头，则不处理
        if (!key.startsWith(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX)) {
            return;
        }
        //eg:
        //计算 fileId 的起始位置
        //key.indexOf("video:play:user:") → 返回 0（前缀从第 0 个字符开始）。
        //"video:play:user:".length() → 假设为 17。
        //userKeyIndex = 0 + 17 = 17（即 fileId 的起始位置）。
        Integer userKeyIndex = key.indexOf(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX) + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX.length();
        //eg:
        //从 key 中截取 fileId，假设 fileId 是固定 20 位长度的字符串（比如 UUID 或哈希值）
        //从 key = "video:play:user:1234567890abcdefghij:deviceA1B2" 中：
        //userKeyIndex = 17（fileId 开始的位置）。
        //userKeyIndex + 20 = 37（fileId 结束的位置）。
        //fileId = key.substring(17, 37) → "1234567890abcdefghij"。
        String fileId = key.substring(userKeyIndex ,userKeyIndex + 10);

        //减少播放数量
        redisComponent.decrementVideoPlayCount(String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE,fileId));
    }
}
