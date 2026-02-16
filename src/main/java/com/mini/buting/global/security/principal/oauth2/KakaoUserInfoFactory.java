package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.SocialProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * <h2>Kakao 전용 사용자 정보 생성 팩토리</h2>
 */
@Component
public class KakaoUserInfoFactory implements OAuth2UserInfoFactory {
    @Override
    public boolean supports(SocialProvider providerName) {
        return SocialProvider.KAKAO.equals(providerName);
    }

    @Override
    public OAuth2UserInfo create(OAuth2User oAuth2User) {
        return new KakaoUserInfo(oAuth2User.getAttributes());
    }
}
