package com.mini.buting.global.security.service;

import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import com.mini.buting.global.util.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * <h2>토큰 블랙리스트 관리 서비스의 구현체</h2>
 *
 * <p>로그아웃 또는 보안 이슈가 발생한 <strong>AccessToken</strong>을 Redis에 블랙리스트로 등록하여 관리함.
 * Stateless인 JWT 특성상 서버에서 강제로 만료시킬 수 없으므로, 유효 시간이 남은 토큰을 Redis에 저장하여 필터 계층에서 검증함.</p>
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final RedisUtils redisUtils;
    private final JwtTokenProvider tokenProvider;

    /**
     * 특정 토큰을 블랙리스트에 등록하여 효력을 상실시킴
     * <p>토큰의 payload를 파싱하여 남은 유효 시간을 계산한 뒤(만료 시각-현재 시각),
     * 해당 시간만큼만 Redis에 TTL로 저장하여 메모리 효율성을 확보한다.</p>
     *
     * @param token 무효화할 JWT Access Token
     */
    @Override
    public void register(String token) {
        Claims claims = tokenProvider.parseClaims(token);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();

        if (remaining > 0) {
            redisUtils.setValue(getRedisKey(token), null, Duration.ofMillis(remaining));
        }
    }

    /**
     * 해당 토큰이 블랙리스트에 등록되어 있는지 확인
     *
     * @param token 확인할 JWT Access Token
     * @return 해당 토큰이 블랙리스트면 {@code true}를 반환, 그렇지 않으면 {@code false}를 반환
     */
    @Override
    public boolean isBlacklisted(String token) {
        return redisUtils.isKeyExist(getRedisKey(token));
    }

    /**
     * 블랙리스트 저장용 Redis 공통 키를 생성
     *
     * @param token JWT 토큰
     * @return "{@code BLACKLIST_ACCESS_PREFIX}{token}" 형태의 키 문자열
     */
    private String getRedisKey(String token) {
        return SecurityConstants.Redis.BLACKLIST_ACCESS_PREFIX + token;
    }
}
