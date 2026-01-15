package com.mini.buting.api.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.buting.api.chat.domain.chatmessage.CachedChatMessage;
import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.dto.response.ChatMessageResponse;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisChatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "chat:room:";
    private static final Integer MAX_CACHE_SIZE = 50;

    public long nextSeq(Long roomId) {
        String key = KEY_PREFIX + roomId + ":seq";
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        if (seq == null) throw new IllegalStateException("Redis INCR failed. key=" + key);
        return seq;
    }

    // 메시지 캐싱
    public void updateCache(ChatMessageDocument message) {
        String redisKey = KEY_PREFIX + message.getRoomId() + ":msgs";

        CachedChatMessage entity = CachedChatMessage.of(message);

        String value;
        try {
            value = objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.warn("chatroom redis caching failed : room[" + message.getRoomId() + "] seq[" + message.getMessageSeq() + "]");
            return;
        }


        stringRedisTemplate.opsForList().leftPush(redisKey, value);
        stringRedisTemplate.opsForList().trim(redisKey, 0, MAX_CACHE_SIZE - 1);

        stringRedisTemplate.expire(redisKey, Duration.ofDays(7));
    }


    // 최근 메시지 조회
    public List<ChatMessageResponse> getRecentMessages(Long roomId){
        String key = KEY_PREFIX + roomId + ":msgs";
        List<String> jsonList = stringRedisTemplate.opsForList().range(key, 0, MAX_CACHE_SIZE - 1);
        if (jsonList == null || jsonList.isEmpty()) return List.of();

        List<CachedChatMessage> result = new ArrayList<>();
        for (String json : jsonList) {
            try {
                result.add(objectMapper.readValue(json, CachedChatMessage.class));
            } catch (Exception e) {
                log.warn("deserialize cached message failed : roomId["+ roomId + "], json=[" + json + "]");
            }
        }

        return result.stream().map(ChatMessageResponse::of).toList();
    }

    public void fillCacheFromMongo(Long roomId, List<ChatMessageDocument> messagesDesc) {
        if (messagesDesc == null || messagesDesc.isEmpty()) return;

        String key = KEY_PREFIX + roomId + ":msgs";

        List<String> values = new ArrayList<>(messagesDesc.size());

        for (int i = messagesDesc.size() - 1; i >= 0; i--) {
            CachedChatMessage cached = CachedChatMessage.of(messagesDesc.get(i));
            try {
                values.add(objectMapper.writeValueAsString(cached));
            } catch (JsonProcessingException e) {
                log.warn("serialize cached message failed : roomId[{}] seq[{}]", roomId, messagesDesc.get(i).getMessageSeq());
            }
        }

        if (values.isEmpty()) return;

        stringRedisTemplate.delete(key);

        stringRedisTemplate.opsForList().leftPushAll(key, values);
        stringRedisTemplate.opsForList().trim(key, 0, MAX_CACHE_SIZE - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(7));
    }

}
