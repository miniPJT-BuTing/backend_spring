package com.mini.buting.api.auth.dto.response;

import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.OAuth2Constants;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <h2>소셜 로그인 응답</h2>
 */
public record OAuth2LoginResponse(
        Integer code,
        Boolean isFirstLogin
) {
    public static OAuth2LoginResponse successLogin() {
        return new OAuth2LoginResponse(BaseResponseStatus.SUCCESS.getCode(), false);
    }
    public String toQueryParams() {
        return UriComponentsBuilder.newInstance()
                .queryParam(OAuth2Constants.Parameter.CODE, code)
                .queryParam(OAuth2Constants.Parameter.IS_SUCCESS, true)
                .queryParam(OAuth2Constants.Parameter.IS_FIRST_LOGIN, isFirstLogin)
                .build().encode().getQuery();
    }
}
