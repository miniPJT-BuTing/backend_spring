package com.mini.buting.global.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis 데이터 조작을 위한 공통 유틸리티 클래스
 */
@Component
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    private final SetOperations<String, Object> setOperations;

    RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.setOperations = redisTemplate.opsForSet();
    }

    public void setValue(String key, Object value) {
        valueOperations.set(key, value);
    }

    /* TTL(Time-To-Live) */
    public void setValue(String key, Object value, Duration duration) {
        valueOperations.set(key, value, duration);
    }

    public boolean setValueIfAbsent(String key, Object value, Duration duration) {
        Boolean result = valueOperations.setIfAbsent(key, value, duration);
        return Boolean.TRUE.equals(result);
    }

    public boolean isKeyExist(String key) {
        return redisTemplate.hasKey(key);
    }

    public Object getValue(String key) {
        return valueOperations.get(key);
    }

    public boolean deleteValue(String key) {
        return redisTemplate.delete(key);
    }

    public void addSetValue(String key, Object value) {
        setOperations.add(key, value);
    }

    public void deleteSetValue(String key, Object value) {
        setOperations.remove(key, value);
    }

    public Set<Object> getSetValue(String key) {
        return setOperations.members(key);
    }

    public boolean isSetMember(String key, Object value) {
        return Boolean.TRUE.equals(setOperations.isMember(key, value));
    }

    public <T> Optional<T> getValue(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(type.cast(value));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
