package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.SocialProvider;

import java.util.Map;

/**
 * <h2>Kakao 사용자 정보 Parser</h2>
 *
 * <p>Kakao로부터 전달받은 {@code Map} 형태의 사용자 속성을 파싱하여 공통 규격으로 반환함.</p>
 * <
 */
public record KakaoUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {
    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get(Key.ID));
    }

    @Override
    public String getProviderEmail() {
        KakaoAccount kakaoAccount = getKakaoAccount();
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    @Override
    public String getProviderName() {
        return SocialProvider.KAKAO.getName();
    }

    public KakaoAccount getKakaoAccount() {
        if (attributes.get(Key.KAKAO_ACCOUNT) instanceof Map<?, ?> accountMap) {
            return new KakaoAccount((Map<String, Object>) accountMap);
        }
        return null;
    }

    /**
     * <h3>kakao_account 정보를 담는 내부 Record</h3>
     * <p>필요한 필드(profile 등)를 이곳에 추가하여 확장 가능</p>
     */
    public record KakaoAccount(Map<String, Object> attributes) {
        public String email() {
            return String.valueOf(attributes.get(Key.EMAIL));
        }
    }

    /**
     * 카카오 전용 속성 키
     */
    private static final class Key {
        private static final String ID = "id";
        private static final String KAKAO_ACCOUNT = "kakao_account";
        private static final String EMAIL = "email";
    }
}
