package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.SocialProvider;

import java.util.Map;

/**
 * <h2>Kakao 사용자 정보 Parser (OIDC 기반)</h2>
 */
public record KakaoUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {
    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get(Key.SUB));
    }

    @Override
    public String getProviderEmail() {
        return String.valueOf(attributes.get(Key.EMAIL));
    }

    @Override
    public String getProviderName() {
        return SocialProvider.KAKAO.getName();
    }

    /**
     * 카카오 전용 속성 키
     */
    private static final class Key {
        private static final String SUB = "sub";
        private static final String EMAIL = "email";
    }
}
