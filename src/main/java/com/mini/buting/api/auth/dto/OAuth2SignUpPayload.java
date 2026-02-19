package com.mini.buting.api.auth.dto;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.principal.oauth2.GuestOAuth2User;
import org.springframework.util.StringUtils;

import java.util.Map;

public record OAuth2SignUpPayload(
        String provider,
        String providerId,
        String email,
        String nickname
) {
    /**
     * Redis에서 조회한 원본 객체를 DTO로 변환
     */
    public static OAuth2SignUpPayload from(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_FAILED);
        }
        return new OAuth2SignUpPayload(
                map.get("provider") instanceof String provider ? provider : null,
                map.get("providerId") instanceof String providerId ? providerId : null,
                map.get("email") instanceof String email ? email : null,
                map.get("nickname") instanceof String nickname ? nickname : null
        );
    }

    public static OAuth2SignUpPayload of(GuestOAuth2User guest) {
        return new OAuth2SignUpPayload(
                guest.provider().getName(),
                guest.providerId(),
                guest.email(),
                guest.nickname()
        );
    }

    public void validateRequiredOrThrow() {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(providerId)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_FAILED);
        }
    }
}
