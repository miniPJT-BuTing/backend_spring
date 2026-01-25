package com.mini.buting.global.security.principal.oauth2;

/**
 * <h2>OAuth2 Provider 별 사용자 정보 추상화 인터페이스</h2>
 * <p>Google, Kakao 등 각 공급자마다 다른 JSON 응답 구조를 통일된 인터페이스로 접근하게 함</p>
 */
public interface OAuth2UserInfo {
    /**
     * @return Provider가 발급한 고유 식별자
     */
    String getProviderId();

    /**
     * @return 소셜 계정에 등록된 이메일
     */
    String getProviderEmail();

    /**
     * @return 소셜 공급자 명칭 (Ex. KAKAO)
     * @see com.mini.buting.api.member.domain.SocialProvider
     */
    String getProviderName();
}
