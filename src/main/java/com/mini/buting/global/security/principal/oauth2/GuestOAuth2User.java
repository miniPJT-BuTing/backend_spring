package com.mini.buting.global.security.principal.oauth2;

import com.mini.buting.api.member.domain.MemberRole;
import com.mini.buting.api.member.domain.SocialProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h2>최초 OAuth2 로그인 사용자 Principal</h2>
 *
 * <p>아직 회원 가입이 완료되지 않은 OAuth2 사용자 정보를 SuccessHandler로 전달하기 위한 Principal.</p>
 * <p>{@link com.mini.buting.global.security.handler.oauth2.strategy.GuestSignUpSuccessStrategy}에서
 * 이 Principal을 감지하여 추가 정보 입력 플로우로 분기함.</p>
 *
 * @param provider   소셜 제공자
 * @param providerId 제공자 내 사용자 식별자
 * @param email      제공자 이메일
 * @param attributes 원본 OAuth2 attributes
 */
public record GuestOAuth2User(
        SocialProvider provider,
        String providerId,
        String email,
        Map<String, Object> attributes
) implements OAuth2User {

    public static GuestOAuth2User of(SocialProvider provider, OAuth2User oAuth2User, OAuth2UserInfo userInfo) {
        return new GuestOAuth2User(
                provider,
                userInfo.getProviderId(),
                userInfo.getProviderEmail(),
                oAuth2User.getAttributes()
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(MemberRole.GUEST.getName()));
    }

    @Override
    public String getName() {
        return providerId;
    }
}
