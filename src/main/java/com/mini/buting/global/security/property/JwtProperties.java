package com.mini.buting.global.security.property;

import java.time.Duration;

/**
 * <h2>JWT 인증 및 만료 설정</h2>
 *
 * @param secretKey  토큰 서명/검증에 사용되는 서버 비밀키
 * @param expireTime 토큰별 만료 시간 설정 객체
 */
public record JwtProperties(
        String secretKey,
        ExpireTime expireTime
) {
    /**
     * 토큰 종류별 만료 시간 정보
     *
     * @param access  AccessToken의 유효 기간
     * @param refresh RefreshToken의 유효 기간
     * @implNote {@link Duration} 타입을 사용하므로, {@code yaml}에서는 "1h", "2m" 등의 양식을 사용하면 됩니다.
     */
    public record ExpireTime(Duration access, Duration refresh) {
    }
}
