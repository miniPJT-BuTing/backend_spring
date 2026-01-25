package com.mini.buting.global.security.token;

import lombok.Builder;

/**
 * <h2>JWT 인증 토큰 응답 레코드</h2>
 *
 * <p>로그인 성공 시 클라이언트에게 반환될 토큰 세트를 담는 DTO
 * {@code record}를 사용하여 데이터 전송의 불변성을 보장하고, Builder 패턴을 통해 생성을 간소화했음</p>
 *
 * @param grantType    인증 타입
 * @param accessToken  인증에 사용되는 AccessToken
 * @param refreshToken 토큰 재발급을 위한 RefreshToken
 */
public record JwtToken(
        String grantType,
        String accessToken,
        String refreshToken
) {
    /**
     * JWT 토큰 생성을 위한 빌더
     *
     * @param grantType    인증 타입
     * @param accessToken  AccessToken
     * @param refreshToken RefreshToken
     */
    @Builder
    public JwtToken(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
