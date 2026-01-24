package com.mini.buting.global.security.token;

import lombok.Builder;

public record JwtToken(
        String grantType,
        String accessToken,
        String refreshToken
) {
    @Builder
    public JwtToken(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
