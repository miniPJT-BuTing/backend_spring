package com.mini.buting.global.security.handler.oauth2.strategy;

import com.mini.buting.api.auth.dto.OAuth2SignUpPayload;
import com.mini.buting.api.auth.dto.response.NeedSignUpResponse;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandlerStrategy;
import com.mini.buting.global.security.principal.oauth2.GuestOAuth2User;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.util.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <h2>최초 소셜 로그인(Guest) 가입 처리 전략</h2>
 *
 * <p>서비스에 처음 방문한 소셜 계정일 경우 실행됨.
 * 가입되지 않은 상태임로 추가 정보 입력을 위한 회원가입 API로 리다이렉트</p>
 */
@Component
@RequiredArgsConstructor
public class GuestSignUpSuccessStrategy implements OAuth2SuccessHandlerStrategy {

    private static final Duration SIGNUP_TOKEN_TTL = Duration.ofMinutes(30);

    private final RedisUtils redisUtils;
    private final SecurityProperties securityProperties;

    @Override
    public boolean supports(Authentication authentication, HttpServletRequest request) {
        return authentication.getPrincipal() instanceof GuestOAuth2User;
    }

    @Override
    public void handle(Authentication authentication,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        GuestOAuth2User guest = (GuestOAuth2User) authentication.getPrincipal();

        String signUpToken = UUID.randomUUID().toString();
        String redisKey = SecurityConstants.Redis.OAUTH2_SIGNUP_PREFIX + signUpToken;

        OAuth2SignUpPayload payload = OAuth2SignUpPayload.of(guest);
        redisUtils.setValue(redisKey, payload, SIGNUP_TOKEN_TTL);

        String redirectUri = securityProperties.oauth2().client().successUrl();
        String query = NeedSignUpResponse.of(guest, signUpToken).toQueryParams();
        response.sendRedirect(redirectUri + "?" + query);
    }
}
