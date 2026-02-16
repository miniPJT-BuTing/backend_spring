package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.SocialProvider;

import java.util.Map;
import java.util.Optional;

/**
 * <h2>Kakao 사용자 정보 Parser</h2>
 *
 * <p>Kakao로부터 전달받은 {@code Map} 형태의 사용자 속성을 파싱하여 공통 규격으로 반환함.</p>
 */
public record KakaoUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {
    @Override
    public String getProviderId() {
        Object id = attributes.get(Key.ID);
        return id == null ? null : String.valueOf(id);
    }

    @Override
    public String getProviderEmail() {
        return Optional.ofNullable(getKakaoAccount())
                .map(KakaoAccount::email)
                .orElse(null);
    }

    @Override
    public String getProviderName() {
        return SocialProvider.KAKAO.getName();
    }

    @Override
    public String getProviderNickname() {
        Object kakaoAccountObj = attributes.get(Key.KAKAO_ACCOUNT);
        if (kakaoAccountObj instanceof Map<?, ?> kakaoAccount) {
            Object profileObj = kakaoAccount.get(Key.PROFILE);
            if (profileObj instanceof Map<?, ?> profile) {
                Object nicknameObj = profile.get(Key.NICKNAME);
                if (nicknameObj != null) {
                    return String.valueOf(nicknameObj);
                }
            }
        }

        Object propsObj = attributes.get(Key.PROPERTIES);
        if (propsObj instanceof Map<?, ?> props) {
            Object nicknameObj = props.get(Key.NICKNAME);
            if (nicknameObj != null) {
                return String.valueOf(nicknameObj);
            }
        }

        return null;
    }

    public KakaoAccount getKakaoAccount() {
        if (attributes.get(Key.KAKAO_ACCOUNT) instanceof Map<?, ?> accountMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) accountMap;

            return new KakaoAccount(m);
        }
        return null;
    }

    /**
     * <h3>kakao_account 정보를 담는 내부 Record</h3>
     * <p>필요한 필드(profile 등)를 이곳에 추가하여 확장 가능</p>
     */
    public record KakaoAccount(Map<String, Object> attributes) {
        public String email() {
            Object email = attributes.get(Key.EMAIL);
            return email == null ? null : String.valueOf(email);
        }
    }

    /**
     * 카카오 전용 속성 키
     */
    private static final class Key {
        private static final String ID = "id";
        private static final String KAKAO_ACCOUNT = "kakao_account";
        private static final String PROPERTIES = "properties";
        private static final String PROFILE = "profile";
        private static final String NICKNAME = "nickname";
        private static final String EMAIL = "email";
    }
}
