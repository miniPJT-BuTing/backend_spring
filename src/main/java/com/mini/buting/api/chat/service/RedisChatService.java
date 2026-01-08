package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisChatService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "chat:room:";

    public long nextSeq(Long roomId) {
        String key = KEY_PREFIX + roomId + ":seq";
        return stringRedisTemplate.opsForValue().increment(key);
    }
    public void updateCache(ChatMessageDocument message) {
        // 최근 메시지 50개 저장

    }


}
