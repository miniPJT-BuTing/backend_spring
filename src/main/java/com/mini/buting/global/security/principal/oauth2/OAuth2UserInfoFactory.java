package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.SocialProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * <h2>Social Provider 별 사용자 정보 생성 전략 인터페이스</h2>
 *
 * @implNote 새로운 소셜 로그인이 추가될 때, 이 인터페이스의 구현체를 생성하여 확장하면 됨
 */
public interface OAuth2UserInfoFactory {
    /**
     * @return 해당 팩토리가 지원하는 소셜 공급자인지 여부
     */
    boolean supports(SocialProvider providerName);

    /**
     * @return oAuth2User Spring Security가 소셜로부터 받아온 원본 유저 객체
     */
    OAuth2UserInfo create(OAuth2User oAuth2User);
}
