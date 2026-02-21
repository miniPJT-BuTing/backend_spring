package com.mini.buting.global.security.filter;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.util.FilterResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.mini.buting.global.security.constant.SecurityConstants.Log;

/**
 * <h2>보안 필터 예외 처리 전용 필터</h2>
 *
 * <p>Spring Security의 Filter Chain에서 발생하는 예외를 가로채어
 * 표준화된 {@link com.mini.buting.global.response.BaseResponse} 형태로 응답을 반환함</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionFilter extends OncePerRequestFilter {
    private final FilterResponseUtils filterResponseUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BaseException e) {
            handleBaseException(request, response, e);
        } catch (Exception e) {
            handleUnexpectedException(request, response, e);
        }
    }

    private void handleBaseException(HttpServletRequest request, HttpServletResponse response, BaseException e) throws IOException {
        SecurityContextHolder.clearContext();

        if (response.isCommitted()) {
            log.warn("{} response already committed. method={}, uri={}, status={}", Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), e.getStatus());
            return;
        }

        log.warn("{} method={}, uri={}, status={}, message={}", Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), e.getStatus(), e.getMessage());

        filterResponseUtils.sendErrorResponse(response, e.getStatus());
    }

    private void handleUnexpectedException(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        SecurityContextHolder.clearContext();

        if (response.isCommitted()) {
            log.error("{} response already committed. method={}, uri={}", Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), e);
            return;
        }

        log.error("{} method={}, uri={}, message={}", Log.LOG_PREFIX, request.getMethod(), request.getRequestURI(), e.getMessage(), e);

        filterResponseUtils.sendErrorResponse(response, BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
