package com.mini.buting.global.security.handler.oauth2;

import com.mini.buting.global.security.constant.OAuth2Constants;
import com.mini.buting.global.security.constant.SecurityConstants.Log;
import com.mini.buting.global.security.property.SecurityProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

/**
 * <h2>OAuth2 인증 성공 통합 Handler</h2>
 *
 * <p>소셜 로그인 성공 후, 등록된 {@link OAuth2SuccessHandlerStrategy} 목록을 순회하며
 * 상황에 맞는 처리 전략을 실행함.</p>
 * <p>
 * <hr/>
 * <h3>주요 역할</h3>
 * <ul>
 * <li>기존 회원 로그인, 신규 회원 가입(Guest), 소셜 계정 연동 등 각 시나리오별 처리 위임</li>
 * <li>전략 실행 중 발생한 비즈니스 예외는 상위 필터로 전달하게 설계되어 있음</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final List<OAuth2SuccessHandlerStrategy> strategies;
    private final SecurityProperties securityProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.warn("{} OAuth2 success response already committed. method={}, uri={}",
                    Log.LOG_PREFIX, request.getMethod(), request.getRequestURI());
            return;
        }

        try {
            for (OAuth2SuccessHandlerStrategy strategy : strategies) {
                if (strategy.supports(authentication, request)) {
                    log.debug("{} Executing OAuth2 success strategy: {}", Log.LOG_PREFIX, strategy.getClass().getSimpleName());
                    strategy.handle(authentication, request, response);
                    return;
                }
            }
            log.error("{} No suitable strategy found for OAuth2 success", Log.LOG_PREFIX);
            redirectFailure(response, 500);
        } catch (Exception e) {
            log.error("{} OAuth2 success handling failed. method={}, uri={}, message={}",
                    Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), e.getMessage(), e);
            if (!response.isCommitted()) {
                redirectFailure(response, 500);
            }
        }
    }

    /**
     * <h3>OAuth2 실패 결과 리다이렉트</h3>
     *
     * @param response HTTP 응답
     * @param code     실패 코드
     * @throws IOException I/O 예외
     */
    private void redirectFailure(HttpServletResponse response, int code) throws IOException {
        String redirectUri = securityProperties.oauth2().client().successUrl();
        String targetUri = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam(OAuth2Constants.Parameter.IS_SUCCESS, false)
                .queryParam(OAuth2Constants.Parameter.CODE, code)
                .build()
                .toUriString();
        response.sendRedirect(targetUri);
    }
}
