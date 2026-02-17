package com.mini.buting.global.security.service;

import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * <h2>RefreshToken 전용 Redis 저장소</h2>
 * <p>RT 저장/조회/삭제 및 키 규칙을 정의</p>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final RedisUtils redisUtils;

    /**
     * Redis에 RefreshToken 저장
     */
    public void save(String memberUuid, String sessionUuid, String refreshToken, Duration ttl) {
        redisUtils.setValue(getKey(memberUuid, sessionUuid), refreshToken, ttl);
    }

    /**
     * Redis에서 RefreshToken 조회
     */
    public Optional<String> get(String memberUuid, String sessionUuid) {
        Object value = redisUtils.getValue(getKey(memberUuid, sessionUuid));
        if (value instanceof String token && StringUtils.hasText(token)) {
            return Optional.of(token);
        }
        return Optional.empty();
    }

    /**
     * Redis에서 RefreshToken 삭제
     */
    public void delete(String memberUuid, String sessionUuid) {
        redisUtils.deleteValue(getKey(memberUuid, sessionUuid));
    }

    private String getKey(String memberUuid, String sessionUuid) {
        return SecurityConstants.Redis.REFRESH_PREFIX + memberUuid + ":" + sessionUuid;
    }
}
