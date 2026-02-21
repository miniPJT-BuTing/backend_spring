package com.mini.buting.global.security.handler;

import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.util.FilterResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <h2>인증 실패(401) 처리 엔트리포인트</h2>
 *
 * <p>Spring Security 인가 단계에서 인증이 필요하나 인증 정보가 없는 경우 호출되어,
 * 표준화된 {@link com.mini.buting.global.response.BaseResponse} 형태로 401 응답을 반환</p>
 * <hr/>
 * <h5>호출 조건</h5>
 * <ul>
 *     <li>{@code authenticated()} 또는 {@code hasRole/hasAuthority} 규칙이 적용된 경로에</li>
 *     <li>인증 객체가 없거나(토큰 누락/인증 실패), 인증이 성립하지 않은 상태로 접근한 경우</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final FilterResponseUtils filterResponseUtils;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.warn("{} response already committed. method={}, uri={}",
                    SecurityConstants.Log.LOG_PREFIX, request.getMethod(), request.getRequestURI());
            return;
        }

        log.warn("{} authentication required. method={}, uri={}, message={}",
                SecurityConstants.Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), ex.getMessage());

        filterResponseUtils.sendErrorResponse(response, BaseResponseStatus.AUTHENTICATION_REQUIRED);
    }
}
