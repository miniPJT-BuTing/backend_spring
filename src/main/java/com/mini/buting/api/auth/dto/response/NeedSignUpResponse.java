package com.mini.buting.api.auth.dto.response;

import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.OAuth2Constants;
import com.mini.buting.global.security.principal.oauth2.GuestOAuth2User;
import org.springframework.web.util.UriComponentsBuilder;

public record NeedSignUpResponse(
        Integer code,
        Boolean isFirstLogin,
        String nickname,
        String providerName,
        String signUpToken
) {
    public static NeedSignUpResponse of(GuestOAuth2User guest, String signupToken) {
        return new NeedSignUpResponse(
                BaseResponseStatus.SUCCESS.getCode(),
                true,
                guest.nickname(),
                guest.provider().getName(),
                signupToken
        );
    }

    public String toQueryParams() {
        return UriComponentsBuilder.newInstance()
                .queryParam(OAuth2Constants.Parameter.CODE, code)
                .queryParam(OAuth2Constants.Parameter.IS_SUCCESS, true)
                .queryParam(OAuth2Constants.Parameter.IS_FIRST_LOGIN, isFirstLogin)
                .queryParam(OAuth2Constants.Parameter.NICKNAME, nickname == null ? "" : nickname)
                .queryParam(OAuth2Constants.Parameter.PROVIDER_NAME, providerName)
                .queryParam(OAuth2Constants.Parameter.SIGNUP_TOKEN, signUpToken)
                .build().encode().getQuery();
    }
}
