package com.mini.buting.global.security.handler.oauth2.strategy;

import com.mini.buting.api.auth.dto.response.OAuth2LoginResponse;
import com.mini.buting.api.auth.service.AuthTokenService;
import com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandlerStrategy;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.property.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <h2>기존 등록 회원 로그인 처리 전략</h2>
 *
 * <p>이미 가입된 소셜 계정 정보가 존재할 경우 실행됨</p>
 * <p>OAuth2 인증 성공 직후 서버 자체 JWT를 구성하고, 프론트 랜딩 페이지로 리다이렉트 처리</p>
 * <hr/>
 * <h5>동작 방식</h5>
 * <ol>
 *     <li>{@link AuthTokenService}를 통해 JWT(Access/Refresh) 발급</li>
 *     <li>RefreshToken은 HttpOnly Cookie로 저장</li>
 *     <li>클라이언트 success URL로 리다이렉트</li>
 * </ol>
 *
 * <hr/>
 * <h5>기타 참고(프론트 연동 시)</h5>
 * <ul>
 *     <li>OAuth2 로그인은 브라우저 리다이렉트 기반이므로, 응답 헤더(Authorization)에 담긴 AccessToken은 프론트에서 읽을 수 없음.</li>
 *     <li>따라서 RefreshToken을 HttpOnly Cookie로만 전달하고, 프론트는 리다이렉트 이후 RT 기반 reissue API를 호출하여 AccessToken을 획득하도록 설계</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class MemberLoginSuccessStrategy implements OAuth2SuccessHandlerStrategy {

    private final SecurityProperties securityProperties;
    private final AuthTokenService authTokenService;

    @Override
    public boolean supports(Authentication authentication, HttpServletRequest request) {
        return authentication.getPrincipal() instanceof AuthUser;
    }

    @Override
    public void handle(Authentication authentication,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        authTokenService.issueJwt(user, response);

        String redirectUri = securityProperties.oauth2().client().successUrl();
        String query = OAuth2LoginResponse.successLogin().toQueryParams();

        response.sendRedirect(redirectUri + "?" + query);
    }
}
