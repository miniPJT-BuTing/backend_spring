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
            // 비즈니스 에외 처리
            writeLog(e, request);
            filterResponseUtils.sendErrorResponse(response, e.getStatus());
        } catch (Exception e) {
            // 예상치 못한 시스템 예외 처리
            writeLog(e, request);
            filterResponseUtils.sendErrorResponse(response, BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void writeLog(Exception e, HttpServletRequest request) {
        String status = (e instanceof BaseException be) ? be.getStatus().toString()
                : "INTERNAL_SERVER_ERROR";
        log.warn("{} [{}] URI: {}, Message: {}",
                Log.LOG_PREFIX,
                e.getClass().getSimpleName(),
                request.getRequestURI(),
                e.getMessage());
    }
}
