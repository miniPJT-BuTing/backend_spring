package com.mini.buting.global.security.handler;

import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.util.FilterResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <h2>인가 실패(403) 처리 핸들러</h2>
 *
 * <p>Spring Security 인가 단계에서 {@link AccessDeniedHandler}이 발생한 경우 호출되며,
 * 표준화된 {@link com.mini.buting.global.response.BaseResponse} 형태로 403 응답을 반환</p>
 * <p>
 * <hr/>
 * <h5>[참고] 호출 조건</h5>
 * <ul>
 *     <li>요청이 인증(Authentication)은 되었으나, 권한이 부족한 경우</li>
 *     <li>{@code hasRole}, {@code hasAuthority} 등의 인가 규칙 위반 시</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final FilterResponseUtils filterResponseUtils;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.warn("{} response already committed. method={}, uri={}",
                    SecurityConstants.Log.LOG_PREFIX, request.getMethod(), request.getRequestURI());
            return;
        }

        log.warn("{} access denied. method={}, uri={}, message={}",
                SecurityConstants.Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), ex.getMessage());
        filterResponseUtils.sendErrorResponse(response, BaseResponseStatus.ACCESS_DENIED);
    }
}
