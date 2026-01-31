package com.mini.buting.global.security.handler.oauth2;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * <h2>OAuth2 인증 성공 통합 Handler</h2>
 *
 * <p>소셜 로그인 성공 후, 등록된 {@link OAuth2SuccessHandlerStrategy} 목록을 순회하며
 * 상황에 맞는 처리 전략을 실행함.</p>
 *
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        for (OAuth2SuccessHandlerStrategy strategy : strategies) {
            if (strategy.supports(authentication, request)) {
                log.debug("{} Executing OAuth2 success strategy: {}", Log.LOG_PREFIX, strategy.getClass().getSimpleName());
                strategy.handle(authentication, request, response);
                return;
            }
        }
        log.error("{} No suitable strategy found for OAuth2 success", Log.LOG_PREFIX);
        throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
